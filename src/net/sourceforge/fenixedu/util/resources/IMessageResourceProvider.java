package net.sourceforge.fenixedu.util.resources;

public interface IMessageResourceProvider {

    public String getMessage(String bundle, String key, String... args);

}
