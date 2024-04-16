package shared;

public class Message {
    public enum Type {
	LOGIN,
	LOGOUT,
	TEXT
    }

    public enum Status {
	SUCCESS,
	FAILURE,
	ERROR,
	INVALID_ARGUMENT
    }

    private Type type;
    private Status status;
    private String content;

    public Message(Type t, Status s, String msg) {
	this.type = t;
	this.status = s;
	this.content = msg;
    }

    public Type getType() {
	return type;
    }

    public Status getStatus() {
	return status;
    }

    public String getContent() {
	return content;
    }
}
