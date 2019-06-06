#import "StoneSDK.h"

@implementation StoneSDK

- (void)setEnvironment:(CDVInvokedUrlCommand*)command {
    NSString* environment = [[command arguments] objectAtIndex:0];
    if ([environment isEqual: @"SANDBOX"]) {
        [STNConfig setEnvironment:STNEnvironmentSandbox];
    } else if([environment isEqual: @"PRODUCTION"]) {
        [STNConfig setEnvironment:STNEnvironmentProduction];
    }
}

- (void)validation:(CDVInvokedUrlCommand*)command {

    // Recebe o Stone Code
    NSString* stoneCodeList = [[command arguments] objectAtIndex:0];

    // Ativando o Stone Code;
    [STNStoneCodeActivationProvider activateStoneCode:stoneCodeList withBlock:^(BOOL succeeded, NSError *error) {
        CDVPluginResult* result;
        if (succeeded) {
            NSString* msg = @"Stone Code ativado com sucesso.";
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:msg];
        } else {
            NSString* msg = error.description;
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:msg];
        }
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

- (void)device:(CDVInvokedUrlCommand*)command {

    // Efetua a conexão com o pinpad
    NSArray *pinpads =[[STNPinPadConnectionProvider new] listConnectedPinpads];
    STNPinpad *pinpad;
    __block UIAlertView *alert;
    __block NSString* msg = @"";
    __block NSString* title = @"";

    if(pinpads.count > 0) {
        pinpad = [pinpads objectAtIndex:0];
        BOOL hasConnected = [[STNPinPadConnectionProvider new] selectPinpad:pinpad];

        if (hasConnected) {
            [STNPinPadConnectionProvider connectToPinpad:^(BOOL succeeded, NSError *error) {
                if (succeeded) {
                    title = @"Ativado!";
                    msg = @"Pareado com sucesso!";
                } else {
                    msg = @"Não foi possivel conectar ao pinpad!";
                    title = @"Oops!";
                }

                alert = [[UIAlertView alloc]
                         initWithTitle: title
                         message:msg
                         delegate:self
                         cancelButtonTitle:@"Ok"
                         otherButtonTitles:nil];
            }];
        } else {
            msg = @"Não foi possivel conectar ao pinpad!";
            title = @"Oops!";

            alert = [[UIAlertView alloc]
                         initWithTitle: title
                         message:msg
                         delegate:self
                         cancelButtonTitle:@"Ok"
                         otherButtonTitles:nil];
        }
    } else {
        msg = @"Não foi possivel conectar ao pinpad!";
        title = @"Oops!";

        alert = [[UIAlertView alloc]
                         initWithTitle: title
                         message:msg
                         delegate:self
                         cancelButtonTitle:@"Ok"
                         otherButtonTitles:nil];
    }
    [alert show];
}

- (void)isDeviceConnected:(CDVInvokedUrlCommand*)command {

  CDVPluginResult* result;
  if ([STNValidationProvider validatePinpadConnection] == YES) {
    NSLog(@"Pinpad está conectado ao celular");
    NSString* msg = @"true";
    result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:msg];
  } else {
    NSLog(@"Pinpad não está conectado ao celular");
    NSString* msg = @"false";
    result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:msg];
  }
  [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)deviceDisplay:(CDVInvokedUrlCommand*)command {

    // Recebe a mensagem
    NSString* message = [[command arguments] objectAtIndex:0];

    [STNDisplayProvider displayMessage:message withBlock:^(BOOL succeeded, NSError *error) {
        CDVPluginResult* result;
        if (succeeded) {
            NSLog(@"Mensagem enviada para pinpad.");
            NSString* msg = @"Mensagem enviada para pinpad.";
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:msg];
        } else {
            NSLog(@"%@", error.description);
            NSString* msg = error.description;
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:msg];
        }
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

- (void)transaction:(CDVInvokedUrlCommand*)command {

    // Recebe o valor do Plugin
    NSString* amount = [[command arguments] objectAtIndex:0];

    /*
     O valor da transação deve ser sempre por CENTAVOS e para isso
     devemos utilizar com um int no objeto da transação;
     */
    float realValue = [self convertToFloat:amount] / 100;
    float centsFromFloatValue = 100 * realValue;
    int justCents = (int) centsFromFloatValue;

    // Iniciando o modelo transaction para efetivar a transacao;
    STNTransactionModel *transaction = [[STNTransactionModel alloc] init];

    // Propriedade Obrigatória, deve conter o valor da transação em centavos. (EX. R$ 56,45 = 5645);
    transaction.amount = [NSNumber numberWithInteger:justCents];
    transaction.merchant = [STNMerchantListProvider listMerchants][0];

    // Recebe o método de pagamento do Plugin
    NSString* method = [[command arguments] objectAtIndex:1];

    // Recebe o numero de parcelas do Plugin
    NSString* instalments = [[command arguments] objectAtIndex:2];

    // Verifica se é DÉBITO ou CRÉDITO.
    if ([method  isEqual: @"DEBIT"]) { // é Débito
        instalments = @"1";
        int intInstalments = [instalments integerValue];

        // Propriedade Obrigatória, define o número de parcelas da transação;
        transaction.instalmentAmount = intInstalments;

        // Propriedade Obrigatória, define o tipo de transação, se é débito ou crédito;
        transaction.type = STNTransactionTypeSimplifiedDebit;

    } else if ([method  isEqual: @"CREDIT"]) { // é Crédito
        // Propriedade Obrigatória, define o tipo de transação, se é débito ou crédito;
        transaction.type = STNTransactionTypeSimplifiedCredit;

        if ([instalments isEqual: @"ONE_INSTALMENT"]) {
            transaction.instalmentAmount = STNTransactionInstalmentAmountOne;
            transaction.instalmentType = STNInstalmentTypeNone;
            NSLog(@"ONE_INSTALMENT");
        } else if ([instalments isEqual: @"TWO_INSTALMENT_NO_INTEREST"]) {
            transaction.instalmentAmount = STNTransactionInstalmentAmountTwo;
            transaction.instalmentType = STNInstalmentTypeMerchant;
            NSLog(@"TWO_INSTALMENT_NO_INTEREST");
        } else if ([instalments isEqual: @"THREE_INSTALMENT_NO_INTEREST"]) {
            transaction.instalmentAmount = STNTransactionInstalmentAmountThree;
            transaction.instalmentType = STNInstalmentTypeMerchant;
            NSLog(@"THREE_INSTALMENT_NO_INTEREST");
        } else if ([instalments isEqual: @"FOUR_INSTALMENT_NO_INTEREST"]) {
            transaction.instalmentAmount = STNTransactionInstalmentAmountFour;
            transaction.instalmentType = STNInstalmentTypeMerchant;
            NSLog(@"FOUR_INSTALMENT_NO_INTEREST");
        } else if ([instalments isEqual: @"FIVE_INSTALMENT_NO_INTEREST"]) {
            transaction.instalmentAmount = STNTransactionInstalmentAmountFive;
            transaction.instalmentType = STNInstalmentTypeMerchant;
            NSLog(@"FIVE_INSTALMENT_NO_INTEREST");
        } else if ([instalments isEqual: @"SIX_INSTALMENT_NO_INTEREST"]) {
            transaction.instalmentAmount = STNTransactionInstalmentAmountSix;
            transaction.instalmentType = STNInstalmentTypeMerchant;
            NSLog(@"SIX_INSTALMENT_NO_INTEREST");
        } else if ([instalments isEqual: @"SEVEN_INSTALMENT_NO_INTEREST"]) {
            transaction.instalmentAmount = STNTransactionInstalmentAmountSeven;
            transaction.instalmentType = STNInstalmentTypeMerchant;
            NSLog(@"SEVEN_INSTALMENT_NO_INTEREST");
        } else if ([instalments isEqual: @"EIGHT_INSTALMENT_NO_INTEREST"]) {
            transaction.instalmentAmount = STNTransactionInstalmentAmountEight;
            transaction.instalmentType = STNInstalmentTypeMerchant;
            NSLog(@"EIGHT_INSTALMENT_NO_INTEREST");
        } else if ([instalments isEqual: @"NINE_INSTALMENT_NO_INTEREST"]) {
            transaction.instalmentAmount = STNTransactionInstalmentAmountNine;
            transaction.instalmentType = STNInstalmentTypeMerchant;
            NSLog(@"NINE_INSTALMENT_NO_INTEREST");
        } else if ([instalments isEqual: @"TEN_INSTALMENT_NO_INTEREST"]) {
            transaction.instalmentAmount = STNTransactionInstalmentAmountTen;
            transaction.instalmentType = STNInstalmentTypeMerchant;
            NSLog(@"TEN_INSTALMENT_NO_INTEREST");
        } else if ([instalments isEqual: @"ELEVEN_INSTALMENT_NO_INTEREST"]) {
            transaction.instalmentAmount = STNTransactionInstalmentAmountEleven;
            transaction.instalmentType = STNInstalmentTypeMerchant;
            NSLog(@"ELEVEN_INSTALMENT_NO_INTEREST");
        } else if ([instalments isEqual: @"TWELVE_INSTALMENT_NO_INTEREST"]) {
            transaction.instalmentAmount = STNTransactionInstalmentAmountTwelve;
            transaction.instalmentType = STNInstalmentTypeMerchant;
            NSLog(@"TWELVE_INSTALMENT_NO_INTEREST");
        } else if ([instalments isEqual: @"TWO_INSTALMENT_WITH_INTEREST"]) {
            transaction.instalmentAmount = STNTransactionInstalmentAmountTwo;
            transaction.instalmentType = STNInstalmentTypeIssuer;
            NSLog(@"TWO_INSTALMENT_WITH_INTEREST");
        } else if ([instalments isEqual: @"THREE_INSTALMENT_WITH_INTEREST"]) {
            transaction.instalmentAmount = STNTransactionInstalmentAmountThree;
            transaction.instalmentType = STNInstalmentTypeIssuer;
            NSLog(@"THREE_INSTALMENT_WITH_INTEREST");
        } else if ([instalments isEqual: @"FOUR_INSTALMENT_WITH_INTEREST"]) {
            transaction.instalmentAmount = STNTransactionInstalmentAmountFour;
            transaction.instalmentType = STNInstalmentTypeIssuer;
            NSLog(@"FOUR_INSTALMENT_WITH_INTEREST");
        } else if ([instalments isEqual: @"FIVE_INSTALMENT_WITH_INTEREST"]) {
            transaction.instalmentAmount = STNTransactionInstalmentAmountFive;
            transaction.instalmentType = STNInstalmentTypeIssuer;
            NSLog(@"FIVE_INSTALMENT_WITH_INTEREST");
        } else if ([instalments isEqual: @"SIX_INSTALMENT_WITH_INTEREST"]) {
            transaction.instalmentAmount = STNTransactionInstalmentAmountSix;
            transaction.instalmentType = STNInstalmentTypeIssuer;
            NSLog(@"SIX_INSTALMENT_WITH_INTEREST");
        } else if ([instalments isEqual: @"SEVEN_INSTALMENT_WITH_INTEREST"]) {
            transaction.instalmentAmount = STNTransactionInstalmentAmountSeven;
            transaction.instalmentType = STNInstalmentTypeIssuer;
            NSLog(@"SEVEN_INSTALMENT_WITH_INTEREST");
        } else if ([instalments isEqual: @"EIGHT_INSTALMENT_WITH_INTEREST"]) {
            transaction.instalmentAmount = STNTransactionInstalmentAmountEight;
            transaction.instalmentType = STNInstalmentTypeIssuer;
            NSLog(@"EIGHT_INSTALMENT_WITH_INTEREST");
        } else if ([instalments isEqual: @"NINE_INSTALMENT_WITH_INTEREST"]) {
            transaction.instalmentAmount = STNTransactionInstalmentAmountNine;
            transaction.instalmentType = STNInstalmentTypeIssuer;
            NSLog(@"NINE_INSTALMENT_WITH_INTEREST");
        } else if ([instalments isEqual: @"TEN_INSTALMENT_WITH_INTEREST"]) {
            transaction.instalmentAmount = STNTransactionInstalmentAmountTen;
            transaction.instalmentType = STNInstalmentTypeIssuer;
            NSLog(@"TEN_INSTALMENT_WITH_INTEREST");
        } else if ([instalments isEqual: @"ELEVEN_INSTALMENT_WITH_INTEREST"]) {
            transaction.instalmentAmount = STNTransactionInstalmentAmountEleven;
            transaction.instalmentType = STNInstalmentTypeIssuer;
            NSLog(@"ELEVEN_INSTALMENT_WITH_INTEREST");
        } else if ([instalments isEqual: @"TWELVE_INSTALMENT_WITH_INTEREST"]) {
            transaction.instalmentAmount = STNTransactionInstalmentAmountTwelve;
            transaction.instalmentType = STNInstalmentTypeIssuer;
            NSLog(@"TWELVE_INSTALMENT_WITH_INTEREST");
        } else {
            NSLog(@"Escolha uma opção válida");
        }
    } else {
        NSLog(@"Opção Inválida");
    }

    // Vamos efetivar a transacao;
    [STNTransactionProvider sendTransaction:transaction withBlock:^(BOOL succeeded, NSError *error) {
        CDVPluginResult* result;
        if (succeeded) {
            NSString* msg = @"Transacao realizada com sucesso!";
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:msg];
        } else {
            NSString* msg = error.description;
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:msg];
        }
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];

}

- (float)convertToFloat:(NSString*)fromFormatedString {
    NSMutableString *textFieldStrValue = [NSMutableString stringWithString:fromFormatedString];

    NSNumberFormatter *numberFormatter = [[NSNumberFormatter alloc] init];
    [numberFormatter setNumberStyle:NSNumberFormatterDecimalStyle];
    [numberFormatter setMaximumFractionDigits:2];
    [numberFormatter setMinimumFractionDigits:2];

    [textFieldStrValue replaceOccurrencesOfString:numberFormatter.groupingSeparator
                                       withString:@""
                                          options:NSLiteralSearch
                                            range:NSMakeRange(0, [textFieldStrValue length])];

    // Muda o separador decimal para ponto caso esteja numa localidade que use vírgula.
    [textFieldStrValue replaceOccurrencesOfString:numberFormatter.decimalSeparator
                                       withString:@"."
                                          options:NSLiteralSearch
                                            range:NSMakeRange(0, [textFieldStrValue length])];

    float textFieldNum = [[NSDecimalNumber decimalNumberWithString:textFieldStrValue] floatValue];

    return textFieldNum;
}

- (void)transactionList:(CDVInvokedUrlCommand*)command {

    NSArray *transactionsList = [STNTransactionListProvider listTransactions];
    NSMutableArray *msg = [NSMutableArray array];

    for (STNTransactionModel *transaction in transactionsList) {
        // Tratamento do amount somente para exibição.
        int centsValue = [transaction.amount intValue];
        float realValue = centsValue*0.01;
        NSString *amount = [NSString stringWithFormat:@"%.02f", realValue];

        // Tratamento do status.
        NSString *shortStatus;
        if ([transaction.statusString isEqual: @"Transação Aprovada"]) {
            shortStatus = @"Aprovada";
        } else if ([transaction.statusString isEqual:@"Transação Cancelada"]) {
            shortStatus = @"Cancelada";
        } else {
            shortStatus = transaction.statusString;
        }

        NSString *date = transaction.dateString;

        NSString *idTransaction = [NSString stringWithFormat: @"R$ %@ %@ %@", amount, shortStatus, date];

        [msg addObject:idTransaction];
    }

    CDVPluginResult* result = [CDVPluginResult
                               resultWithStatus:CDVCommandStatus_OK
                               messageAsArray:msg];

    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)transactionCancel:(CDVInvokedUrlCommand*)command {
    NSArray *transactions = [STNTransactionListProvider listTransactions];
    NSString* indexTransactionCancel = [[command arguments] objectAtIndex:0];
    NSString *transactionLastCharacter = [indexTransactionCancel substringFromIndex: [indexTransactionCancel length] - 1];
    int value = [transactionLastCharacter integerValue];

    STNTransactionModel *transactionInfoProvider = [transactions objectAtIndex:value];

    [STNCancellationProvider cancelTransaction:transactionInfoProvider withBlock:^(BOOL succeeded, NSError *error) {
        CDVPluginResult* result;
        if (succeeded) {
            NSString* msg = @"Transação cancelada!";
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:msg];
        } else {
            NSString* msg = error.description;
            result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:msg];
        }
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];

}

@end
