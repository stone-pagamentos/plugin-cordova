<!---    
#    license: Copyright (c) 2017 Stone Pagamentos
#    
#             Permission is hereby granted, free of charge, to any person obtaining a copy
#             of this software and associated documentation files (the "Software"), to deal
#             in the Software without restriction, including without limitation the rights
#             to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
#             copies of the Software, and to permit persons to whom the Software is
#             furnished to do so, subject to the following conditions:
#    
#             The above copyright notice and this permission notice shall be included in all
#             copies or substantial portions of the Software.
#    
#             THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
#             IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
#             FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
#             AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
#             LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
#             OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
#             SOFTWARE.
-->

# Demo Cordova plugin

Este plugin é um exemplo de como utilizar a SDK da Stone no Cordova/Ionic.

## Exemplo de como utilizar

- [demo-sdk-cordova](https://github.com/stone-pagamentos/demo-sdk-cordova)

## Documentação da SDK

- [Android](https://stone-pagamentos.gitbooks.io/sdk-android/)
- [iOS](https://github.com/stone-pagamentos/sdk-ios-v2)

## Plataformas Suportadas

- Android
- iOS

## Instalação das SDKs

### Android

A SDK do Android já está no plugin, mas também pode ser encontrada [aqui](https://github.com/stone-pagamentos/sdk-android-V2)

### iOS

A SDK do iOS pode ser encontrada [aqui](https://github.com/stone-pagamentos/sdk-ios-v2/releases)

## Instalação do Plugin

    $ cordova plugin add https://github.com/stone-pagamentos/plugin-cordova.git

## API

### Métodos

- [stone_sdk.validation](#validation)
- [stone_sdk.device](#device)
- [stone_sdk.deviceSelected](#deviceselected)
- [stone_sdk.transaction](#transaction)
- [stone_sdk.transactionList](#transactionlist)
- [stone_sdk.transactionCancel](#transactioncancel)
- [stone_sdk.displayMessage](#displayMessage)

## validation

Ativa o Stonecode.

    stone_sdk.validation(stonecode, success, failure);
    
### Descrição

A função `validation` é responsável pela ativação do Stonecode na SDK. Caso o Stonecode seja ativado com sucesso, a função irá chamar o callback de sucesso, caso contrário, será chamado o callback de falha.

### Parâmetros

- __stonecode__: Identificador único na Stone.
- __success__: Callback de sucesso.
- __failure__: Callback de falha.

## device

Lista os dispositivos pareados.

    stone_sdk.device(success, failure);
    
### Descrição

A função `device` é responsável por listar todos os dispositivos pareados. Se a listagem ocorrer com sucesso, a função irá chamar o callback de sucesso, caso contrário, será chamado o callback de falha.

### Parâmetros

- __success__: Callback de sucesso.
- __failure__: Callback de falha.

## deviceSelected

Lista os dispositivos pareados.

    stone_sdk.deviceSelected(pinpadName_macAddress, success, failure);

### Descrição

A função `deviceSelected` é responsável por conectar o Pinpad selecionado na SDK. No primeiro argumento, ela recebe uma string com o nome do Pinpad e o seu macAddres separados por um "_". Se o dispositivo selecionado for um Pinpad, a função irá chamar o callback de sucesso e irá ativar a coneão bluetooth, caso contrário, será chamado o callback de falha.

### Parâmetros

- __pinpadName_macAddress__: Nome do pindad e o seu macAddress .
- __success__: Callback de sucesso.
- __failure__: Callback de falha.

## transaction

Realiza um transação.

    stone_sdk.transaction(amount, paymentMethod, instalments, success, failure);

### Descrição

A função `transaction` é responsável por realizar uma transação. No primeiro argumento, ela recebe uma string com o montante a ser transacionado. No segundo argumento ela recebe o método de pagamento (crédito ou debito). No terceiro argumento ela recebe a quantidade de parcelas escolhida. Se a transação for bem sucedida, a função irá chamar o callback de sucesso , caso contrário, será chamado o callback de falha.

### Parâmetros

- __amount__: Valor a ser transacionado.
- __paymentMethod__: Método de pagamento selecionado.
- __instalments__: Número de parcelas selecionada.
- __success__: Callback de sucesso.
- __failure__: Callback de falha.

## transactionList

Exibe a lista de transações.

    stone_sdk.transactionList(success, failure);

### Descrição

A função `transactionList` é responsável por exibir a lista de transações ocorridas. Se houverem transações efetuadas, a função irá chamar o callback de sucesso e irá exibir a lista com as transações, caso contrário, será chamado o callback de falha.

### Parâmetros

- __success__: Callback de sucesso.
- __failure__: Callback de falha.

## transactionCancel

Exibe a lista de transações.

    stone_sdk.transactionCancel(idTransaction_amountTransaction_statusTransaction, success, failure);

### Descrição

A função `transactionCancel` é responsável pelo cancelamento da transação selecionada. O primeiro argumento recebe uma string com o id da transação, o valor transacionado e o status da transação separados por "_". Se o cancelamento ocorrer com sucesso, a função irá chamar o callback de sucesso, caso contrário, será chamado o callback de falha.

### Parâmetros

- __idTransaction_amountTransaction_statusTransaction__: Id da transação, valor transacionado e o seu status.
- __success__: Callback de sucesso.
- __failure__: Callback de falha.

## displayMessage

Exibe uma mensagem no pinpad conectado

    stone_sdk.displayMessage(deviceName, deviceMacAddress, messageToDisplay, success, failure);
    
### Descrição

A função `displayMessage` é responsável pelo envio de uma mensagem para o display do pinpad.

### Parâmetros

- __deviceName__: Nome do dispositivo utilizado na conexão.
- __deviceMacAddress__: Endereço Mac do dispotivo.
- __messageToDisplay__: Mensagem a ser exibida no display.
- __success__: Callback de sucesso.
- __failure__: Callback de falha.