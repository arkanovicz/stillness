package stillness;

import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
import org.apache.velocity.exception.ResourceNotFoundException;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;

/**
 * Created by IntelliJ IDEA.
 * User: marwan
 * Date: 8 juil. 2007
 * Time: 16:57:37
 * To change this template use File | Settings | File Templates.
 */
public class StillnessResourceLoader extends FileResourceLoader {

    /**
     * On a juste besoin de surcharger cette methode. Pour le reste ca doit etre bon
     * @param templateName
     * @return
     * @throws ResourceNotFoundException
     */
    public InputStream getResourceStream(String templateName)
        throws ResourceNotFoundException
    {
        InputStream is = super.getResourceStream(templateName);

        boolean normalize = true; // TODO
        // a ce niveau is est forcement initialisé ou on a leve une exception
        if(normalize) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String src = "";
                String l = null;
                while ((l = br.readLine()) != null) src += l;
                src = StillnessUtil.normalize(src);
                // On ne peut que passer par le ByteArray...
                return new ByteArrayInputStream(src.getBytes());
            } catch (Exception e) {

            }
        }
        return is;
    }

}
