package chat;

public class ChatResponse {

    String sender;
    String reciever;
    String message;
    String type;
    String date;
    String time;
    boolean isseen;
    String id;

    public ChatResponse() {
    }

    public ChatResponse(String sender, String reciever, String message,String type,String date,String time,boolean isseen,String id) {
        this.sender = sender;
        this.reciever = reciever;
        this.message = message;
        this.type=type;
        this.date =date;
        this.time=time;
        this.isseen = isseen;
        this.id=id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReciever() {
        return reciever;
    }

    public void setReciever(String reciever) {
        this.reciever = reciever;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
