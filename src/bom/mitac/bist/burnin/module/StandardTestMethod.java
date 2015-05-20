package bom.mitac.bist.burnin.module;

/**
 * Created with IntelliJ IDEA.
 * User: xiaofeng.liu
 * Date: 14-3-12
 * Time: 下午6:09
 */
public interface StandardTestMethod {

    boolean classSetup();

    boolean testSetup();

    boolean testBegin();

    boolean testEnd();

    boolean testCleanup();

    boolean classCleanup();

    void start();

    void stop();

}
