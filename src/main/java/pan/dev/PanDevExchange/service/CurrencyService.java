package pan.dev.PanDevExchange.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import pan.dev.PanDevExchange.entity.Currency;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Scanner;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    public static String start(){
        return "Good afternoon!\n" +
                "This is a task from PanDev company for currency conversion.\n" +
                "There are two types of conversion." +
                " \n 1. USD => KZT and 2. KZT => USD"+
                "\n Сhoose which currency you have: "+ "\n" +
                "/USD" + "\n" +
                "/KZT";
    }

    public static String typeUsdOrKZT(String type){
        if (type.equals("USD")) return "Enter your conversion sum $";
        if (type.equals("KZT")) return "Enter your conversion sum Tenge";
        return type;
    }

    public static String currencyConverter(int sum, String type) throws IOException, ParseException {
        URL url = new URL("https://api.exchangerate.host/convert?from=USD&to=KZT");
        Scanner scanner = new Scanner((InputStream) url.getContent());
        String result = "";
        while (scanner.hasNext()) {
            result += scanner.nextLine();
        }
        JSONObject object = new JSONObject(result);
        Currency currency = new Currency();
        currency.setDate(object.getString("date"));
        currency.setResult(object.getLong("result"));
        double finalResult;
        if (type.equals("USD")) {
            return Double.valueOf(currency.getResult()*sum)+" tenge \n According to today's currency: "+currency.getDate();
        }else {
            return Double.valueOf(sum/currency.getResult())+"$ \n According to today's currency: "+currency.getDate();
        }

    }



}
