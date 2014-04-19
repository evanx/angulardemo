package iolfeed;

import vellum.provider.VellumProvider;

/**
 *
 * @author evanx
 */
public class FeedsProvider {
    
    public static FeedsContext getContext() {
        return VellumProvider.provider.get(FeedsContext.class);
    }
    
    public static ContentStorage getStorage() { 
        return VellumProvider.provider.get(ContentStorage.class);
    }
    

}
