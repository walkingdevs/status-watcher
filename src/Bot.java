import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

class Bot extends TelegramLongPollingBot {
   public String getBotToken() {
//      return "735589559:AAG6t-VWfx1anbC-IZfx7TmsVhaKr8vI0FU";
      return System.getenv("token");
   }

   public String getBotUsername() {
//      return "wat4erbot";
      return System.getenv("username");
   }

   void fire(String text) {
      SendMessage message = new SendMessage();
      message.setChatId(
//              "-324961315"
              System.getenv("chatId")
      );
      message.setText(text);
      try {
         execute(message);
      } catch (TelegramApiException e) {
         e.printStackTrace();
      }
   }

   public void onUpdateReceived(Update update) {
   }
}