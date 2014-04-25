/*
 */

package iolfeed;

import java.text.ParseException;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import vellum.jx.JMap;

/**
 *
 * @author evanx
 */
public class FeedsTest {

    JMap feedsProperties = new JMap();
    FeedsContext context = new FeedsContext(new TaskManager(), new ContentStorage(), feedsProperties);

    public FeedsTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void parseGallery() throws ParseException {
        String line = "\t <a href=\"/polopoly_fs/iol-mot-apr20-audi-tt-concept-a-1.1678226!/image/449629179.jpg_gen/derivatives/landscape_600/449629179.jpg\">";
        Assert.assertTrue(ArticleTask.galleryImageLinkPattern.matcher(line).find());
    }        
    
    @Test
    public void parseImageJpeg() throws ParseException {
        Pattern imageLinkPattern
            = Pattern.compile("^\\s*<img src=\"(/polopoly_fs/\\S*/[0-9]*.jpe?g)\"\\s*");
        String line = "\t <img src=\"/polopoly_fs/to-nikita-city-e1-1.1679090!/image/545198014.jpeg_gen/derivatives/box_300/545198014.jpg\" alt=\"TO nikita_CITY_E1\" title=\"\"  class=\"pics\"/>";
        Assert.assertTrue(imageLinkPattern.matcher(line).find());
    }        

    @Test
    public void fixAccent() {
        Assert.assertEquals("euro", FeedsUtil.unicodeHtmlMap.get('€'));
        Assert.assertEquals("risqué", FeedsUtil.accentHtmlToUnicode("risqu&#233;"));
    }

    /*
    À &Agrave; 192
    È &Egrave; 200
    Ì &Igrave; 204	
    Ò &Ograve; 210	
    Ù &Ugrave; 217
    à &agrave; 224	
    è &egrave; 232
    ì &igrave; 236
    ò &ograve; 242
    ù &ugrave; 249
    Á &Aacute; 193
    É &Eacute; 201	
    Í &Iacute; 205	
    Ó &Oacute; 211	
    Ú &Uacute; 218
    Ý &Yacute; 221
    á &aacute; 225
    é &eacute; 233	
    í &iacute; 237	
    ó &oacute; 243	
    ú &uacute; 250	
    ý &yacute; 253
    Â &Acirc; 194	
    Ê &Ecirc; 202	
    Î &Icirc; 206	
    Ô &Ocirc; 212	
    Û &Ucirc; 219
    â &acirc; 226	
    ê &ecirc; 234	
    î &icirc; 238	
    ô &ocirc; 244	
    û &ucirc; 251
    Ã &Atilde; 195
    Ñ &Ntilde; 209	
    Õ &Otilde; 213 
    ã &atilde; 227
    ñ &ntilde; 241	
    õ &otilde; 245
    Ä &Auml; 196	
    Ë &Euml; 203	
    Ï &Iuml; 207	
    Ö &Ouml; 214	
    Ü &Uuml; 220	
    Ÿ &Yuml; 159
    ä &auml; 228	
    ë &euml; 235	
    ï &iuml; 239	
    ö &ouml; 246	
    ü &uuml; 252	
    ÿ &yuml; 255    
    
    ¢ &cent; 162
    £ &pound; 163
    ¥ &yen; 165
    € &euro; 8364
    « &laquo; 171
    » &raquo; 187
    ‹ &lsaquo; 8249
    › &rsaquo; 8250
    ‚ &sbquo; 8218
    „ &bdquo; 8222
    “ &ldquo; 8220
    ” &rdquo; 8221
    ‘ &lsquo; 8216
    ’ &rsquo; 8217
    
    ® &reg; 174
    © &copy; 169
    ™ &trade; 153
    ¶ &para; 182
    • &bull; 149
    · &middot; 183
    § &sect; 167
    – &ndash; 150
    — &mdash; 151   
    
    */
    
}
