package pan.dev.PanDevExchange.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import pan.dev.PanDevExchange.config.BotConfig;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class ServiceSpeechToText {

    private final BotConfig botConfig;
    private static final String REQUEST = "https://asr.yandex.net/?" +
            "uuid=ajekiec038rl02h4kuth&" +
            "key=AQVN1JOYAaN1AKJKe7EM-6-D0FPl6Jm1JXd-j_cr&" +
            "topic=queries";

    public static String recognizeText(byte[] data) throws IOException, ParserConfigurationException, SAXException {
        String xmlResponse = "";
        HttpURLConnection connection = ((HttpURLConnection) new URL(REQUEST).openConnection());
        try {
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "audio/x-pcm;bit=16;rate=16000");
            connection.setRequestProperty("User-Agent", "BoBa");
            connection.setRequestProperty("Host", "asr.yandex.net");
            connection.setRequestProperty("Content-Length", "" + data.length);
            connection.setRequestProperty("Transfer-Encoding", "chunked");
            connection.setUseCaches(false);

            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.write(data);
            }
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()))) {
                String decodedString;
                while ((decodedString = in.readLine()) != null) {
                    xmlResponse += decodedString;
                }
            }
            return XmlParser.parseYandexResponse(xmlResponse);
        } finally {
            connection.disconnect();
        }
    }



}
