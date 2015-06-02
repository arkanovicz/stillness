package stillness;

import org.apache.velocity.runtime.RuntimeLogger;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;

public class VelocityLogAdapter implements RuntimeLogger,LogChute {

	private RuntimeServices _rsvc = null;

	// LogChute methods
	
	public void init(RuntimeServices rs) {
		_rsvc = rs;
	}

	public void log(int level, String message) {
		Logger.log(level + 1, message);
	}
	
	public void log(int level, String message, Throwable t) {
		Logger.log(message, t);
	}

	public boolean isLevelEnabled(int level) {
		return (level + 1 >= Logger.mLogLevel);
	}

	// RuntimeLogger methods
	
    public void debug(java.lang.Object message) {
        Logger.debug(message.toString());
    }

    public void info(java.lang.Object message) {
        Logger.info(message.toString());
    }

    public void warn(java.lang.Object message) {
        Logger.warn(message.toString());
    }

    public void error(java.lang.Object message) {
        Logger.error(message.toString());
    }
}
