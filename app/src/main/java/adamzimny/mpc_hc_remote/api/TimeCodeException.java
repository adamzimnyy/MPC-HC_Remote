package adamzimny.mpc_hc_remote.api;

/**
 * Exception thrown by {@link TimeCode}.
 */
public class TimeCodeException extends Exception
{
  private static final long serialVersionUID = -4510604601242231041L;

  public TimeCodeException(String message)
  {
    super(message);
  }
}
