package main.java.transLayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.CopyOnWriteArrayList;
import main.java.util.Timer;
import org.apache.log4j.Logger;

public abstract class TransportProtocol {

  private static final Logger log = Logger.getLogger(TransportProtocol.class.getName());

  private ILinkLayer iLinkLayer;
  private IDataReceiveListener iDataReceiveListener;

  protected static final short chunkSize = 20;
  protected long startTime;
  protected long endTime;
  protected Timer timer;
  protected boolean debugMode;
  protected long countOfSent;
  /* A thread-safe variant of ArrayList, doesn't throw ConcurrentModificationException. */
  protected CopyOnWriteArrayList<Thread> threads;

  // injecting link layer as dependency in constructor facilitates isolating layers and testing
  public TransportProtocol(ILinkLayer iLinkLayer, IDataReceiveListener iDataReceiveListener) {
    this.iLinkLayer = iLinkLayer;
    this.iDataReceiveListener = iDataReceiveListener;
    iLinkLayer.subscribeReceiveListener(iDataReceiveListener);
    countOfSent = 0;
  }

  /**
   * transport protocol on top of the provided link-layer that is capable of reliable transmission
   * of the data having arbitrary size.
   *
   * @param dataPacket the data to be sent
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  public void transmitData(String dataPacket, boolean useInputStream, boolean debug)
      throws IOException, InterruptedException {

    threads = new CopyOnWriteArrayList<>();
    debugMode = debug;

    if (useInputStream || dataPacket.equals(null) || dataPacket.equals(null)) {
      /* Enter data using BufferedReader */
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      /* Reading data using readLine */
      dataPacket = reader.readLine();
    }

    /* length of the string calculated */
    int dataLength = dataPacket.length();

    /* string converted to character array */
    char[] stringToCharArray = dataPacket.toCharArray();

    /* counter to set how many chunks of chunkSize bytes data has to be sent */
    int counterOfChunks = dataLength / chunkSize;

    /* to get length of last chunk of data */
    int leftdata = dataLength % chunkSize;

    /* print to logs */
    String divider = "=========================================================";
    if (debugMode) {
      log.info("======================DEBUG=MODE=========================");
      /* for debugging */
      Runtime.getRuntime().traceMethodCalls(true);
    } else log.info(divider);

    /* instantiate timer */
    instTimer();

    /* instantiate handlers */
    instHandlers();

    log.info("\n\n" + divider + "\n" + getTimestamp() + ": Establishing connection ...");

    /* start the timer */
    timer.start();

    /* perform the handshake */
    connect();

    log.info(getTimestamp() + ": Connection established." + "\n" + divider + "\n\n\n" + divider);

    if (!debugMode) log.info(getTimestamp() + ": Transferring data ... ");

    startTime = System.currentTimeMillis();

    // for loop to sent chunkSize bytes data at a time till the last set
    // to iterate data with the number od chunkSize bytes of data
    for (int i = 0; i < counterOfChunks; i++) {

      // string to be formed for chunkSize bytes of data to be sent at a time
      String datatobesent = "";

      // for loop to form chunkSize bytes of data to be sent
      for (int j = i * chunkSize; j < (i + 1) * chunkSize; j++) {
        datatobesent = datatobesent + String.valueOf(stringToCharArray[j]);
      }

      // converting datachunk of chunkSize length created to be converted to bytes as i byte is 1
      // character
      byte[] data = datatobesent.getBytes();

      // while loop to sent the data the loop will be running till the chunk of data is sent when
      // true value is received it exits
      if (this.iLinkLayer != null) {
        while (!iLinkLayer.send(data)) {
          log.info("data set " + (counterOfChunks + 1) + " is not sent");
        }
        log.info("data set " + (counterOfChunks + 1) + " is  sent");
      }
    }

    // for loop to sent the last chunk of data
    String datatobesent = "";
    for (int i = counterOfChunks * chunkSize; i < (counterOfChunks * chunkSize + leftdata); i++) {
      datatobesent = datatobesent + String.valueOf(stringToCharArray[i]);
    }

    // converting datachunk of chunkSize length created to be converted to bytes
    byte[] data = datatobesent.getBytes();

    // while loop to sent data. the loop will be running till the chunk of data is sent when
    // true value is received it exits
    if (this.iLinkLayer != null) {
      while (!iLinkLayer.send(data)) {
        log.info("data set " + (counterOfChunks + 1) + " is not sent");
      }
    }

    endTime = System.currentTimeMillis();

    log.info(divider + "\n\n\n" + divider + "\n" + getTimestamp() + ": Closing connection...");

    log.info("data set " + (counterOfChunks + 1) + " is  sent");
    logSent(data);

    /* close the connection */
    log.info(divider + "\n\n\n" + divider + "\n" + getTimestamp() + ": Closing connection...");
    disconnect();

    /* close timer */
    timer.shutdown();

    log.info(divider + "\n\n");
  }

  /** Instantiates the synchronized ACKnowledgement, handler threads for data. */
  protected abstract void instHandlers();

  /** Starts the synchronized ACKnowledgement and handle threads for data. */
  protected abstract void startHandlers();

  /** Performs the handshake necessary to synchronize and open connection to the remote host. */
  protected abstract void connect();

  /** Performs the handshake to close connection to the remote host. */
  protected abstract void disconnect();

  /** Prints the data transfer statistics upon termination. */
  protected abstract void printStats();

  /** Instantiates the Timer. */
  private void instTimer() {
    timer = new Timer(debugMode);
    timer.setName("Timer Thread");
    threads.add(timer);
  }

  /**
   * Kills the program and displays error message to standard error.
   *
   * @param msg : The message to be displayed before terminating.
   */
  protected void kill(String msg, Exception e) {
    log.error(msg);
    System.exit(-1);
  }

  /**
   * Returns a timestamp with millisecond accuracy.
   *
   * @return : String formatted timestamp.
   */
  protected String getTimestamp() {
    return new SimpleDateFormat("[HH:mm:ss:SSS] ").format(Calendar.getInstance().getTime());
  }

  /**
   * Logs the contents of the data payload object to the logger debug level.
   *
   * @param data : The data payload to be sent reliably.
   */
  protected synchronized void logSent(byte[] data) {
    log.debug(getTimestamp() + ": Data sent with length: " + data.length);
    countOfSent++;
  }

  public CopyOnWriteArrayList<Thread> getThreads() {
    return threads;
  }

  public ILinkLayer getiLinkLayer() {
    return iLinkLayer;
  }

  public void setiLinkLayer(ILinkLayer iLinkLayer) {
    this.iLinkLayer = iLinkLayer;
  }

  public IDataReceiveListener getiDataReceiveListener() {
    return iDataReceiveListener;
  }

  public void setiDataReceiveListener(IDataReceiveListener iDataReceiveListener) {
    this.iDataReceiveListener = iDataReceiveListener;
  }

  public long getCountOfSent() {
    return countOfSent;
  }
}
