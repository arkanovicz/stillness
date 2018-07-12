package com.republicate.stillness;

import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
import org.apache.velocity.exception.ResourceNotFoundException;

import java.io.*;

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
    public Reader getResourceReader(String templateName, String encoding)
        throws ResourceNotFoundException
    {
        Reader reader = super.getResourceReader(templateName, encoding);

        boolean normalize = true; // TODO
        // a ce niveau is est forcement initialisé ou on a leve une exception
        if(normalize) {
            try {
                BufferedReader br = new BufferedReader(reader);
                String src = "";
                String l = null;
                while ((l = br.readLine()) != null) src += l;
                src = StillnessUtil.normalize(src);
                return new StringReader(src);
            } catch (Exception e) {

            }
        }
        return reader;
    }

}
