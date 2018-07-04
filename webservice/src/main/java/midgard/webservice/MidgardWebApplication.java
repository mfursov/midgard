package midgard.webservice;

import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;

@Slf4j
public class MidgardWebApplication extends WebApplication {

    protected void init() {
        log.error("----------======== Starting Service Application ========----------");
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return HelloPage.class;
    }

    protected void onDestroy() {
        log.debug("onDestroy!");
    }

}
