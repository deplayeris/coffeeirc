package mod.deplayer.coffeechat.coffeeirc.logger;

public interface BaseLogger
{

    final String pathLogFile = "";

    void setLogger(String name);

    void log(String message);

    void log(String message, String type);

    void outpLogFile(String pathLogFile, String pathOutpLogFile);

    void outpLogMsg(String msg, String pathOutpLogFile);

    void outpLogMsg(String msg, String pathOutpLogFile, String type);

    void searchLog(String searchToken);

};
