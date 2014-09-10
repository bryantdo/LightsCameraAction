/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bryantd.lightscameraaction;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author bryantd
 */
public class Utilities {
    
    static final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd'_'HH:mm:ss.SSSS"); 
    
    private Utilities() {}
    
    public static StringBuffer timeStamp(String toStamp) {
        return new StringBuffer("[" + dtf.print(DateTime.now()) + "] " + toStamp);
    }
}
