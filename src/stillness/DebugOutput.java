package stillness;

import stillness.node.RNode;

import java.io.*;
import java.util.Vector;

/**
 * This class contains methods to create the debug output
 *
 * @author Cluade Brisson
 */
public class DebugOutput {

    protected PrintWriter _output;

    public DebugOutput(PrintWriter output) {
        _output = output;
        _output.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
        _output.println("<html><head><title>Stillness Debug Output</title></head><body>");
    }

    /**
     * Log a successful scrape
     * @param rtl The reverse template source
     * @param value The value retrieved from scraping
     */
    public void logValue(String rtl,String value) {
        _output.println(textToHtml(rtl)
            + "[=\"<font color="+StillnessConstants._scrapeColor+">"+textToHtml(value)
            +"</font>\"]");
    }

    /**
     *
     * @param rtl
     */
	public void logFailure(String rtl) {
        _output.println("<font color="+StillnessConstants._mismatchColor+">"
        +textToHtml(rtl)+"</font>");
    }

    /**
     * Log a successful regexp directive
     * @param rtl Reverse template source
     * @param listGroup Vector of groups
     */
    public void logRegexp(String rtl, Vector listGroup) {
		String text = ""+ textToHtml(rtl);
		if (listGroup != null) {
			text += "[\"";
			for (int i=0; i<listGroup.size(); i++) {
				text += "Group"+i+"=<font color="+StillnessConstants._scrapeColor+">"
					+textToHtml((String)listGroup.elementAt(i))+"</font>";
				if (i != listGroup.size()-1) text += ", ";
			}
			text += "\"]";
		}

        _output.println(text);
    }

    /**
     * Log a failed match or regexp
     * @param rtl reverse template source
     * @param orangeTxt maximum match in the template
     * @param redTxt part of the template that didn't match
     */
    public void logMismatch(String rtl, String orangeTxt, String redTxt) {
		String text = ""+ textToHtml(rtl) + "[=\"";
		if (orangeTxt != null) text += "<font color="+StillnessConstants._subMatchColor+">"+textToHtml(orangeTxt)+"</font>";
		if (redTxt != null) text += "<font color='red'>"+textToHtml(redTxt)+"</font>";
		text += "\"]";
        _output.println(text);
    }
	
    /**
     * Simply write the text
     * @param text text to write
     * @param newLine indicate if we must have a new line BEFORE writting the text
     */
    public void logText(String text, boolean newLine) {
		if (newLine) _output.println("<br>");
        _output.println(textToHtml(text));
    }

    // TODO infos...
    /**
     * Indicate end of foreach directive (first mismatch)
     */
    public void logEndOfLoop() {
        _output.println("<font color="+StillnessConstants._endLoopColor+">loopMissed</font>");
    }

    /**
     * Log failure in if statement
     * @param condition the condition that didn't match
     */
    public void logFailedCondition(String condition) {
        _output.println("<font color="+StillnessConstants._mismatchColor+">#if ("
        +textToHtml(condition)+")</font>");
    }

    /**
     * Log exception
     */
    public void logError(Exception e) {
        _output.println(" <font color='red'>Error: "+textToHtml(e.getMessage())+"</font>");
    }

    public void endDebug() {
        _output.println("</body></html>");
		_output.flush();
    }

    // TODO: � revoir, incomplet et inefficace
    public static String textToHtml(String inText)
    {
        // does almost nothing for now...
        inText = strReplace(inText,"&","&amp;");
        inText = strReplace(inText,"'","&acute;");
        inText = strReplace(inText,"\"","&quot;");
        inText = strReplace(inText,"<","&lt;");
        inText = strReplace(inText,">","&gt;");
        inText = strReplace(inText,"\"","&#34;");

        return inText;
    }

    protected static String strReplace(String inTarget, String inOldPattern, String inNewPattern)
    {
            if (inTarget == null)
                    return null;

            if (inOldPattern==null || inOldPattern.length()==0 || inNewPattern==null) return inTarget;

            StringBuffer buff = new StringBuffer();
            int previous=0,offset=0,length=inOldPattern.length();

            while ( (offset=inTarget.indexOf(inOldPattern,previous)) !=-1)
            {
                    buff.append(inTarget.substring(previous,offset));
                    buff.append(inNewPattern);
                    previous=offset+length;
            }
            buff.append(inTarget.substring(previous));

            return buff.toString();
    }


}
