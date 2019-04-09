package main.java.transLayer;

import java.io.IOException;

public interface ILinkLayer {
  /**
   * Sends data to the remote part
   *
   * @param data data to send
   */
  boolean send(byte[] data) throws IOException, InterruptedException;

  /**
   * Subscription for data listeners
   *
   * @param listener listener object
   */
  void subscribeReceiveListener(IDataReceiveListener listener);
}
