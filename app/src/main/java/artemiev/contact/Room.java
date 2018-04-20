package artemiev.contact;

import java.util.ArrayList;

public class Room {
    public Room(int id, String name, int pc, int pl) {
        ID = id;
        Name = name;
        playerCount = pc;
        playerLimit = pl;
    }

    private int cnt;

    public void attachPlayer(String nickName) {
        if (players == null) {
            players = new ArrayList<>();
        }
        //if (playerCount < playerLimit) {
            players.add(new Player(nickName, cnt));
            cnt++;
            playerCount++;
        //}
    }

    int ID;
    String Name;
    int playerCount;
    int playerLimit;
    ArrayList<Player> players;
}
