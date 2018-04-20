package artemiev.contact;

public interface ClientInterface {
    public void register(String nickName);
    public void fetchRoomList();
    public void fetchRoomData(int roomID);
    public void createRoom();
    public void enterRoom(int roomID);
    public void leaveRoom();
}
