package test.java.transLayer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import main.java.transLayer.IDataReceiveListener;
import main.java.transLayer.ILinkLayer;
import main.java.transLayer.TransportProtocol;
import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TransportProtocolTest extends TransportProtocol {
  private TransportProtocol protocol;

  public TransportProtocolTest() {
    super(mock(ILinkLayer.class), mock(IDataReceiveListener.class));
  }

  @Before
  public void init() {
    BasicConfigurator.configure();
    protocol = this;
  }

  @Test
  public void whenAssertingCountOfSent_thenReturnIncremented() {
    try {
      ILinkLayer iLinkLayer = this.getiLinkLayer();
      byte[] arr = "Morse code".getBytes();
      when(iLinkLayer.send(arr)).thenReturn(true);
      protocol.transmitData("Morse code", false, false);
      assertEquals(1, protocol.getCountOfSent());
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @After
  public void destroy() {
    Mockito.reset(protocol.getiLinkLayer());
    Mockito.reset(protocol.getiDataReceiveListener());
  }

  @Override
  protected void instHandlers() {}

  @Override
  protected void startHandlers() {}

  @Override
  public void connect() {}

  @Override
  public void disconnect() {}

  @Override
  public void printStats() {}
}
