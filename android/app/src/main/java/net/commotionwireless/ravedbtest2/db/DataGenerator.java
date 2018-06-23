package net.commotionwireless.ravedbtest2.db;

import net.commotionwireless.ravedbtest2.db.entity.RaveMessageEntity;
import net.commotionwireless.ravedbtest2.db.entity.RaveNodeEntity;
import net.commotionwireless.ravedbtest2.model.RaveNode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Generates data to pre-populate the database
 */
public class DataGenerator {

    private static final String[] FIRST = new String[]{
            "Unknown", "New", "Excited", "Random", "Unseen"};
    private static final String[] SECOND = new String[]{
            "Bear", "Cat", "Dog", "Raccoon"};
    private static final String[] MESSAGES = new String[]{
            "Test message 1", "Test message 2", "Test message 3",
            "Test message 4", "Test message 5", "Test message 6"};

    public static List<RaveNodeEntity> generateRaveNodes() {
        List<RaveNodeEntity> raveNodes = new ArrayList<>(FIRST.length * SECOND.length);
        for (int i = 0; i < FIRST.length; i++) {
            for (int j = 0; j < SECOND.length; j++) {
                RaveNodeEntity raveNode = new RaveNodeEntity();
                raveNode.setAlias(FIRST[i] + " " + SECOND[j]);
                raveNode.setAddress(FIRST.length * i + j + 1);
                raveNodes.add(raveNode);
            }
        }
        return raveNodes;
    }

    public static List<RaveMessageEntity> generateMessagesForNodes(
            final List<RaveNodeEntity> raveNodes) {
        List<RaveMessageEntity> raveMessages = new ArrayList<>();
        Random rnd = new Random();

        for (RaveNode raveNode : raveNodes) {
            int messagesNumber = rnd.nextInt(5) + 1;
            for (int i = 0; i < messagesNumber; i++) {
                RaveMessageEntity raveMessage = new RaveMessageEntity();
                raveMessage.setNodeAddress(raveNode.getAddress());
                raveMessage.setContents(MESSAGES[i] + " for " + raveNode.getAlias());
                raveMessage.setTimestamp(new Date(System.currentTimeMillis()
                        - TimeUnit.DAYS.toMillis(messagesNumber - i) + TimeUnit.HOURS.toMillis(i)));
                raveMessages.add(raveMessage);
            }
        }

        return raveMessages;
    }
}
