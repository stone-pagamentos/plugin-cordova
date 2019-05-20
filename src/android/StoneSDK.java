package br.com.stone.cordova.sdk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.widget.Toast;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import stone.application.StoneStart;
import stone.application.enums.ErrorsEnum;
import stone.application.enums.InstalmentTransactionEnum;
import stone.application.enums.TypeOfTransactionEnum;
import stone.application.interfaces.StoneCallbackInterface;
import stone.database.transaction.TransactionDAO;
import stone.database.transaction.TransactionObject;
import stone.providers.ActiveApplicationProvider;
import stone.providers.BluetoothConnectionProvider;
import stone.providers.DisplayMessageProvider;
import stone.providers.CancellationProvider;
import stone.providers.TransactionProvider;
import stone.user.UserModel;
import stone.utils.GlobalInformations;
import stone.utils.PinpadObject;
import stone.utils.Stone;
import stone.utils.StoneTransaction;
import stone.cache.ApplicationCache;
import stone.environment.Environment;

public class StoneSDK extends CordovaPlugin {

    static BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private static final String DEVICE = "device";
    private static final String DEVICE_SELECTED = "deviceSelected";
    private static final String DEVICE_DISPLAY = "deviceDisplay";
    private static final String IS_DEVICE_CONNECTED = "isDeviceConnected";
    private static final String SET_ENVIRONMENT = "setEnvironment";
    private static final String TRANSACTION = "transaction";
    private static final String TRANSACTION_CANCEL = "transactionCancel";
    private static final String TRANSACTION_LIST = "transactionList";
    private static final String VALIDATION = "validation";

    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        if (action.equals(DEVICE)) {
            turnBluetoothOn();
            bluetoothList(callbackContext);
            return true;
        } else if (action.equals(DEVICE_SELECTED)) {
            bluetoothSelected(data, callbackContext);
            return true;
        } else if (action.equals(DEVICE_DISPLAY)) {
            bluetoothDisplay(data, callbackContext);
            return true;
        } else if (action.equals(TRANSACTION)) {
            transaction(data, callbackContext);
            return true;
        } else if (action.equals(TRANSACTION_CANCEL)) {
            transactionCancel(data, callbackContext);
            return true;
        } else if (action.equals(TRANSACTION_LIST)) {
            transactionList(callbackContext);
            return true;
        } else if (action.equals(IS_DEVICE_CONNECTED)) {
            bluetoothIsConnected(callbackContext);
            return true;
        } else if (action.equals(VALIDATION)) {
            List<UserModel> user = StoneStart.init(this.cordova.getActivity());
            if (user == null) {
                stoneCodeValidation(data, callbackContext);
                return true;
            } else {
                Toast.makeText(StoneSDK.this.cordova.getActivity(), "StoneCode ja cadastrado", Toast.LENGTH_SHORT).show();
                callbackContext.success();
                return true;
            }
        } else if (action.equals(SET_ENVIRONMENT)) {
            setEnvironment(data);
            return true;
        } else {
            return false;
        }
    }

    public void turnBluetoothOn() {
        try {
            mBluetoothAdapter.enable();
            do {
            } while (!mBluetoothAdapter.isEnabled());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bluetoothList(CallbackContext callbackContext) throws JSONException {
        // Lista de Pinpads para passar para o BluetoothConnectionProvider.
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        JSONArray arrayList = new JSONArray();

        // Lista todos os dispositivos pareados.
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String name = device.getName();
                String address = device.getAddress();
                arrayList.put(name + "_" + address);
            }
            callbackContext.success(arrayList);
        }
    }

    private void bluetoothIsConnected(CallbackContext callbackContext) throws JSONException {
        // Lista de Pinpads para passar para o BluetoothConnectionProvider.
        Boolean isConnected = GlobalInformations.isConnectedToPinpad();
        String result = String.valueOf(isConnected);

        callbackContext.success(result);
    }


    private void bluetoothSelected(JSONArray data, final CallbackContext callbackContext) throws JSONException {
        // Pega o pinpad selecionado.
        String arrayList = data.getString(0);

        String[] parts = arrayList.split("_");

        String name = parts[0];
        String macAddress = parts[1];

        PinpadObject pinpad = new PinpadObject(name, macAddress, false);

        // Passa o pinpad selecionado para o provider de conexao bluetooth.
        BluetoothConnectionProvider bluetoothConnectionProvider = new BluetoothConnectionProvider(StoneSDK.this.cordova.getActivity(), pinpad);
        bluetoothConnectionProvider.setDialogMessage("Criando conexao com o pinpad selecionado"); // Mensagem exibida do dialog.
        bluetoothConnectionProvider.setWorkInBackground(false); // Informa que havera um feedback para o usuario.
        bluetoothConnectionProvider.setConnectionCallback(new StoneCallbackInterface() {

            public void onSuccess() {
                Toast.makeText(StoneSDK.this.cordova.getActivity(), "Pinpad conectado", Toast.LENGTH_SHORT).show();
                callbackContext.success();
            }

            public void onError() {
                Toast.makeText(StoneSDK.this.cordova.getActivity(), "Erro durante a conexao. Verifique a lista de erros do provider para mais informacoes", Toast.LENGTH_SHORT).show();
                callbackContext.error("Erro durante a conexao. Verifique a lista de erros do provider para mais informacoes");
            }

        });
        bluetoothConnectionProvider.execute(); // Executa o provider de conexao bluetooth.
    }

    private void bluetoothDisplay(JSONArray data, final CallbackContext callbackContext) throws JSONException {
        // Pega o pinpad selecionado.
        String displayMessage = data.getString(0);

        DisplayMessageProvider displayMessageProvider = new DisplayMessageProvider(StoneSDK.this.cordova.getActivity(), displayMessage, Stone.getPinpadFromListAt(0));
        /* displayMessageProvider.setDialogMessage("Criando conexao com o pinpad selecionado"); // Mensagem exibida do dialog. */
        displayMessageProvider.setWorkInBackground(false); // Informa que haverá um feedback para o usuário.
        displayMessageProvider.setConnectionCallback(new StoneCallbackInterface() {

            public void onSuccess() {
                /* Toast.makeText(StoneSDK.this.cordova.getActivity(), "Pinpad conectado", Toast.LENGTH_SHORT).show(); */
                callbackContext.success();
            }

            public void onError() {
                /* Toast.makeText(StoneSDK.this.cordova.getActivity(), "Erro durante a conexao. Verifique a lista de erros do provider para mais informacoes", Toast.LENGTH_SHORT).show(); */
                callbackContext.error("Testando agora erro");
            }

        });
        displayMessageProvider.execute();
    }


    private void stoneCodeValidation(JSONArray data, final CallbackContext callbackContext) throws JSONException {
        List<String> stoneCodeList = new ArrayList<String>();

        // Adicione seu Stonecode abaixo, como string.
        stoneCodeList.add(data.getString(0)); // coloque seu Stone Code aqui

        final ActiveApplicationProvider activeApplicationProvider = new ActiveApplicationProvider(this.cordova.getActivity(), stoneCodeList);
        activeApplicationProvider.setDialogMessage("Ativando o aplicativo...");
        activeApplicationProvider.setDialogTitle("Aguarde");

        //Essa linha estava gerando erro no build, porem nao sei a necessidade dessa parte... Parece que ja tem implementado acima...
        //activeApplicationProvider.setActivity(this.cordova.getActivity());

        activeApplicationProvider.setWorkInBackground(false); // informa se este provider ira rodar em background ou nao
        activeApplicationProvider.setConnectionCallback(new StoneCallbackInterface() {

            /* Sempre que utilizar um provider, intancie esta Interface.
             * Ela ira lhe informar se o provider foi executado com sucesso ou nao
             */

            /* Metodo chamado se for executado sem erros */
            public void onSuccess() {
                Toast.makeText(StoneSDK.this.cordova.getActivity(), "Ativado com sucesso, iniciando o aplicativo", Toast.LENGTH_SHORT).show();
                callbackContext.success("Ativado com sucesso");
            }

            /* Metodo chamado caso ocorra alguma excecao */
            public void onError() {
                Toast.makeText(StoneSDK.this.cordova.getActivity(), "Erro na ativacao do aplicativo, verifique a lista de erros do provider", Toast.LENGTH_SHORT).show();
                callbackContext.error(activeApplicationProvider.getListOfErrors().toString());
            }

        });
        activeApplicationProvider.execute();
    }

    private void setEnvironment(JSONArray data) throws JSONException {
        String env = data.getString(0);
        Stone.setEnvironment(Environment.valueOf(env));
    }

    private void transactionList(CallbackContext callbackContext) {
        // acessa todas as transacoes do banco de dados
        TransactionDAO transactionDAO = new TransactionDAO(StoneSDK.this.cordova.getActivity());

        // cria uma lista com todas as transacoes
        List<TransactionObject> transactionObjects = transactionDAO.getAllTransactionsOrderByIdDesc();

        // exibe todas as transacoes (neste caso valor e status) para o usuario
        JSONArray arrayList = new JSONArray();

        for (TransactionObject list : transactionObjects) {
            JSONObject obj = new JSONObject();

            try{
                obj.put("idFromBase", String.valueOf(list.getIdFromBase()));
                obj.put("amount",  list.getAmount());
                obj.put("requestId",   String.valueOf(list.getRequestId()));
                obj.put("emailSent",   String.valueOf(list.getEmailSent()));
                obj.put("timeToPassTransaction",   String.valueOf(list.getTimeToPassTransaction()));
                obj.put("initiatorTransactionKey",   String.valueOf(list.getInitiatorTransactionKey()));
                obj.put("recipientTransactionIdentification",   String.valueOf(list.getRecipientTransactionIdentification()));
                obj.put("cardHolderNumber",   String.valueOf(list.getCardHolderNumber()));
                obj.put("cardHolderName",   String.valueOf(list.getCardHolderName()).trim());
                obj.put("date",   String.valueOf(list.getDate()));
                obj.put("time",   String.valueOf(list.getTime()));
                obj.put("aid",   String.valueOf(list.getAid()));
                obj.put("arcq",   String.valueOf(list.getArcq()));
                obj.put("authorizationCode",   String.valueOf(list.getAuthorizationCode()));
                obj.put("iccRelatedData",   String.valueOf(list.getIccRelatedData()));
                obj.put("transactionReference",   String.valueOf(list.getTransactionReference()));
                obj.put("actionCode",   String.valueOf(list.getActionCode()));
                obj.put("commandActionCode",   String.valueOf(list.getCommandActionCode()));
                obj.put("pinpadUsed",   String.valueOf(list.getPinpadUsed()));
                obj.put("saleAffiliationKey",   String.valueOf(list.getSaleAffiliationKey()));
                obj.put("cne",   String.valueOf(list.getCne()));
                obj.put("cvm",   String.valueOf(list.getCvm()));
                obj.put("serviceCode",   String.valueOf(list.getServiceCode()));
                obj.put("entryMode",   String.valueOf(list.getEntryMode()));
                obj.put("cardBrand",   String.valueOf(list.getCardBrand()));
                obj.put("instalmentTransaction",   String.valueOf(list.getInstalmentTransaction()));
                obj.put("transactionStatus",   String.valueOf(list.getTransactionStatus()));
                obj.put("instalmentType",   String.valueOf(list.getInstalmentType()));
                obj.put("typeOfTransactionEnum",   String.valueOf(list.getTypeOfTransactionEnum()));
                obj.put("cancellationDate",   String.valueOf(list.getCancellationDate()));

                arrayList.put(obj);

            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        callbackContext.success(arrayList);
    }

    private void transactionCancel(JSONArray data, final CallbackContext callbackContext) throws JSONException {
        System.out.println("Opcao Selecionada Cancel");

        String transactionCode = data.getString(0);
        System.out.println("optSelected: " + transactionCode);

        // Pega o id da transacao selecionada.
        String[] parts = transactionCode.split("_");

        String idOptSelected = parts[0];
        System.out.println("idOptSelected: " + idOptSelected);

        //l�gica do cancelamento
        final int transacionId = Integer.parseInt(idOptSelected);

        final CancellationProvider cancellationProvider = new CancellationProvider(StoneSDK.this.cordova.getActivity(), transacionId, Stone.getUserModel(0));
        cancellationProvider.setWorkInBackground(false); // para dar feedback ao usuario ou nao.
        //cancellationProvider.setDialogMessage("Cancelando...");
        cancellationProvider.setConnectionCallback(new StoneCallbackInterface() { // chamada de retorno.
            public void onSuccess() {
                Toast.makeText(StoneSDK.this.cordova.getActivity(), cancellationProvider.getMessageFromAuthorize(), Toast.LENGTH_SHORT).show();
                callbackContext.success("Cancelado com sucesso");
            }

            public void onError() {
                Toast.makeText(StoneSDK.this.cordova.getActivity(), "Um erro ocorreu durante o cancelamento com a transacao de id: " + transacionId, Toast.LENGTH_SHORT).show();
                callbackContext.error(cancellationProvider.getListOfErrors().toString() + " Erro ocorreu durante o cancelamento da transacao de id: " + transacionId);
            }
        });
        cancellationProvider.execute();
    }

    private void transaction(JSONArray data, final CallbackContext callbackContext) throws JSONException {

        String amount = data.getString(0);
        final  String success_message = data.getString(3);
        System.out.println("getAmount: " + amount);

        // Cria o objeto de transacao. Usar o "GlobalInformations.getPinpadFromListAt"
        // significa que devera estar conectado com ao menos um pinpad, pois o metodo
        // cria uma lista de conectados e conecta com quem estiver na posicao "0".
        TransactionObject transaction = new TransactionObject();

        // A seguir deve-se popular o objeto.
        transaction.setAmount(amount);
        transaction.setEmailClient(null);
        transaction.setRequestId(null);
        transaction.setUserModel(Stone.getUserModel(0));

        // Verifica a forma de pagamento selecionada.
        String method = data.getString(1);
        System.out.println("getMethod: " + method);

        // Numero de parcelas
        String instalments = data.getString(2);
        System.out.println("getInstalments: " + instalments);

        if (method.equals("DEBIT")) {
            transaction.setInstalmentTransactionEnum(InstalmentTransactionEnum.getAt(0));
            transaction.setTypeOfTransaction(TypeOfTransactionEnum.DEBIT);
        } else if (method.equals("CREDIT")) {
            // Informa a quantidade de parcelas.
            transaction.setInstalmentTransactionEnum(InstalmentTransactionEnum.valueOf(instalments));
            transaction.setTypeOfTransaction(TypeOfTransactionEnum.CREDIT);
        } else {
            System.out.println("Empty Payment Method");
        }

        // Processo para envio da transacao.
        final TransactionProvider provider = new TransactionProvider(StoneSDK.this.cordova.getActivity(), transaction, Stone.getUserModel(0), Stone.getPinpadFromListAt(0));

        provider.setWorkInBackground(true);

        provider.setConnectionCallback(new StoneCallbackInterface() {
            public void onSuccess() {
                // acessa todas as transacoes do banco de dados
                TransactionDAO transactionDAO = new TransactionDAO(StoneSDK.this.cordova.getActivity());

                // cria uma lista com todas as transacoes
                List<TransactionObject> transactionObjects = transactionDAO.getAllTransactionsOrderByIdDesc();

                // exibe todas as transacoes (neste caso valor e status) para o usuario
                JSONArray arrayList = new JSONArray();

                //Transforma a lista e objetos em json
                for (TransactionObject list : transactionObjects) {

                    JSONObject obj = new JSONObject();

                    try{
                        obj.put("idFromBase", String.valueOf(list.getIdFromBase()));
                        obj.put("amount",  list.getAmount());
                        obj.put("requestId",   String.valueOf(list.getRequestId()));
                        obj.put("emailSent",   String.valueOf(list.getEmailSent()));
                        obj.put("timeToPassTransaction",   String.valueOf(list.getTimeToPassTransaction()));
                        obj.put("initiatorTransactionKey",   String.valueOf(list.getInitiatorTransactionKey()));
                        obj.put("recipientTransactionIdentification",   String.valueOf(list.getRecipientTransactionIdentification()));
                        obj.put("cardHolderNumber",   String.valueOf(list.getCardHolderNumber()));
                        obj.put("cardHolderName",   String.valueOf(list.getCardHolderName()));
                        obj.put("date",   String.valueOf(list.getDate()));
                        obj.put("time",   String.valueOf(list.getTime()));
                        obj.put("aid",   String.valueOf(list.getAid()));
                        obj.put("arcq",   String.valueOf(list.getArcq()));
                        obj.put("authorizationCode",   String.valueOf(list.getAuthorizationCode()));
                        obj.put("iccRelatedData",   String.valueOf(list.getIccRelatedData()));
                        obj.put("transactionReference",   String.valueOf(list.getTransactionReference()));
                        obj.put("actionCode",   String.valueOf(list.getActionCode()));
                        obj.put("commandActionCode",   String.valueOf(list.getCommandActionCode()));
                        obj.put("pinpadUsed",   String.valueOf(list.getPinpadUsed()));
                        obj.put("saleAffiliationKey",   String.valueOf(list.getSaleAffiliationKey()));
                        obj.put("cne",   String.valueOf(list.getCne()));
                        obj.put("cvm",   String.valueOf(list.getCvm()));
                        obj.put("serviceCode",   String.valueOf(list.getServiceCode()));
                        obj.put("entryMode",   String.valueOf(list.getEntryMode()));
                        obj.put("cardBrand",   String.valueOf(list.getCardBrand()));
                        obj.put("instalmentTransaction",   String.valueOf(list.getInstalmentTransaction()));
                        obj.put("transactionStatus",   String.valueOf(list.getTransactionStatus()));
                        obj.put("instalmentType",   String.valueOf(list.getInstalmentType()));
                        obj.put("typeOfTransactionEnum",   String.valueOf(list.getTypeOfTransactionEnum()));
                        obj.put("cancellationDate",   String.valueOf(list.getCancellationDate()));

                        arrayList.put(obj);

                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }

                //retorna a ultima transacao efetuada
                try{
                    callbackContext.success(arrayList.getJSONObject(0));
                }catch (JSONException e){
                    e.printStackTrace();
                }

                //Mostra o toast com a mensagem personalizada
                Toast.makeText(StoneSDK.this.cordova.getActivity(), success_message, Toast.LENGTH_SHORT).show();
            }

            public void onError() {
                Toast.makeText(StoneSDK.this.cordova.getActivity(), provider.getMessageFromAuthorize(), Toast.LENGTH_SHORT).show();
                callbackContext.error(provider.getListOfErrors().toString());
            }
        });
        provider.execute();
    }

}
