package pan.dev.PanDevExchange.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Voice;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import pan.dev.PanDevExchange.config.BotConfig;
import pan.dev.PanDevExchange.dao.MessageRepository;
import pan.dev.PanDevExchange.entity.Messages;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

@Component
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final CurrencyService currencyService;
    private final MessageRepository messageRepository;

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    String savedType = null;
    @Override
    public void onUpdateReceived(Update update) {

        if(update.hasMessage() && update.getMessage().hasText()){
            String messageText = update.getMessage().getText();
            Messages messages = new Messages(null,messageText);
            messageRepository.save(messages);
            long chatId =update.getMessage().getChatId();
            String response = null;
            switch (messageText) {
                case "/start":
                    response = currencyService.start();
                    sendMessage(chatId,response);
                    break;
                case "/USD":
                    response = currencyService.typeUsdOrKZT("USD");
                    savedType = "USD";
                    sendMessage(chatId,response);
                    break;
                case "/KZT":
                    response = currencyService.typeUsdOrKZT("KZT");
                    savedType = "KZT";
                    sendMessage(chatId,response);
                    break;
                default:
                    String rempResult = update.getMessage().getText();
                    Integer toInt = null;
                    try {
                        toInt = Integer.parseInt(rempResult);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    if (toInt instanceof Integer && toInt != null ){
                        try {
                            response = currencyService.currencyConverter(toInt,savedType);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                        sendMessage(chatId,response);
                    } else {
                        Voice voice = update.getMessage().getVoice();
                        Message incomingMessage = update.getMessage();
                        if (voice == null && incomingMessage != null) {
                            response = "Unsupported numbers or symbols. \n" +
                                "Start again: "+
                                "  /start";
                            sendMessage(chatId,response);
                        } else {
                            System.out.println("Это работает или нет");
                            String responseMessageText = "***ТЕХНИЧЕСКАЯ ОШИБКА***";
                            try {
                                URL fileURL = getFileURL(incomingMessage.getVoice());
                                byte[] audioData = safeDownloadAndConvert(fileURL);
                                responseMessageText = ServiceSpeechToText.recognizeText(audioData);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                sendReplyMessage(incomingMessage, responseMessageText);
                            }
                        }
                    }
            }
        }
    }

    private void sendMessage(Long chatId, String textToSend){
        SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(textToSend);
            try {
                execute(sendMessage);
            }catch (TelegramApiException e){

            }
    }

    private URL getFileURL(Voice containerMessage) throws TelegramApiException, MalformedURLException {
        GetFile getFile = new GetFile();
        getFile.setFileId(containerMessage.getFileId());
        return new URL(execute(getFile).getFileUrl(getBotToken()));
    }

    private synchronized byte[] safeDownloadAndConvert(URL fileURL) throws IOException {
        String originalFileName = "voiceMessage.oga";
        FileUtils.copyURLToFile(fileURL, new File(originalFileName));
        return FFmpegWrapper.convertToWAV(originalFileName);
    }

    private void sendReplyMessage(Message replyToMessage, String text) {
        try {
            SendMessage sendMessage = new SendMessage();
                    sendMessage.enableMarkdown(true);
                    sendMessage.setChatId(replyToMessage.getChatId().toString());
                    sendMessage.setReplyToMessageId(replyToMessage.getMessageId());
                    sendMessage.setText(text);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }



}
