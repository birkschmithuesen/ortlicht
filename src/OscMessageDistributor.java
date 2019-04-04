
import java.util.ArrayList;
import oscP5.OscMessage;

//////////////////////////////////////////////////////////////////////////
// distributes the incoming messages 
//////////////////////////////////////////////////////////////////////////
class OscMessageDistributor {
  static ArrayList<OscMessageSink> allInstances=new ArrayList<OscMessageSink>();        ///< freshly constructed instances are added to this list to enable easy handling of message distribution

  //register any MessageSink in the distributer
  public static void registerAdress(String _adress, OscMessageSink _oscMessageSink) { 
    allInstances.add(_oscMessageSink);
  }

  //distributes the incomming message to all Instances
  public static void distributeMessage(OscMessage _theMessage) {
    for (int i=0; i<allInstances.size(); i++) {
      OscMessageSink theOscMessageSink=allInstances.get(i);
      theOscMessageSink.digestMessage(_theMessage);
    }
  }
}