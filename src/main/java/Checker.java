import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.servicebus.*;
import com.microsoft.windowsazure.services.servicebus.models.*;
import java.util.ArrayList;


public class Checker {
    private  ServiceBusContract service;
    private Configuration config;

    public Checker(){

        config = ServiceBusConfiguration.configureWithSASAuthentication(
                "smarcamera",
                "RootManageSharedAccessKey",
                "h9zP+sPjaennM/CI3rHJzoy+ymsSGJkcJM0/csNp7Vw=",
                ".servicebus.windows.net"

        );

        //Create a connection to the service
        service = ServiceBusService.create(config);
        readVehicleMessages();

    }

    public void readVehicleMessages(){
        try
        {
            ReceiveMessageOptions opts = ReceiveMessageOptions.DEFAULT;
            opts.setReceiveMode(ReceiveMode.PEEK_LOCK);


            while(true)  {
                ReceiveQueueMessageResult resultQM =
                        service.receiveQueueMessage("SpeedingVehicles");
                BrokeredMessage message = resultQM.getValue();
                if (message != null && message.getMessageId() != null)
                {
                    System.out.println("MessageID: " + message.getMessageId());
                    // Display the queue message.
                    System.out.print("From queue: ");
                    byte[] b = new byte[200];
                    String s = null;
                    int numRead = message.getBody().read(b);
                    ArrayList<String> output = new ArrayList<String>();
                    while (-1 != numRead)
                    {
                        s = new String(b);
                        s = s.trim();
                        output.add(s);


                        System.out.print(s);
                        numRead = message.getBody().read(b);
                    }

                    String my = new String();
                    for(String x : output){
                        my += x;
                    }


                    JsonObject object = new JsonParser().parse(my).getAsJsonObject();
                    isVehicleStolen(object.get("registration").getAsString());

                    System.out.println();
                    System.out.println("Custom Property: " +
                            message.getProperty("MyProperty"));
                    // Remove message from queue.
                    System.out.println("Deleting this message.");
                    service.deleteMessage(message);
                }
                else
                {
                    System.out.println("Finishing up - no more messages.");
                    break;
                    // Added to handle no more messages.
                    // Could instead wait for more messages to be added.
                }
            }
        }
        catch (ServiceException e) {
            System.out.print("ServiceException encountered: ");
            System.out.println(e.getMessage());

        }
        catch (Exception e) {
            System.out.print("Generic exception encountered: ");
            System.out.println(e.getMessage());

        }
    }

    public static boolean isVehicleStolen(String vehicleRegistration) throws InterruptedException {
        Thread.sleep(5000);
        return (Math.random() < 0.95);
    }

}
