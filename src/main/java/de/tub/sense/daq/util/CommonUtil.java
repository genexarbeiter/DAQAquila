package de.tub.sense.daq.util;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CommonUtil {

    public static final String CLIENT_URL_KEY = "c2mon.client.jms.url";

    public static final String DAQ_URL_KEY = "c2mon.daq.jms.url";

    public static final String DAQ_PROCESS_NAME_KEY = "c2mon.daq.name";

    public static final String SOURCE_DATA_TYPE_KEY = "sourceDataType";

    private Path oBasePath;

    private URL oMainClassProtectionDomainSourceCodeLocation;

    CommonUtil(URL mainClassDomainSourceLocation) {
        setMainClassProtectionDomainSourceCodeLocationURL(mainClassDomainSourceLocation);
    }

    protected URL getMainClassProtectionDomainSourceCodeLocationURL() {
        return this.oMainClassProtectionDomainSourceCodeLocation;
    }

    protected void setMainClassProtectionDomainSourceCodeLocationURL(URL mainClassDomainSourceLocation) {
        this.oMainClassProtectionDomainSourceCodeLocation = mainClassDomainSourceLocation;
        this.oBasePath = determineBasePathFromURL(mainClassDomainSourceLocation);
    }

    protected Path getBasePath() {
        return this.oBasePath;
    }

    protected Path determineBasePathFromURL(URL mainClassDomainSourceLocation) {
        try {
            URI uri = mainClassDomainSourceLocation.toURI();
            String s = uri.getRawSchemeSpecificPart();
            if (s.contains(".jar!"))
                return Paths.get(URI.create(s.substring(0, s.indexOf("!")))).getParent();
            if (s.endsWith(".jar")) {
                if (!s.startsWith(uri.getScheme()))
                    s = uri.getScheme().concat("://").concat(s);
                return Paths.get(URI.create(s)).getParent();
            }
            return (new File("")).getAbsoluteFile().toPath();
        } catch (URISyntaxException uRISyntaxException) {
            return null;
        }
    }

    protected void checkC2monClientHostConnection(String brokerHost, String c2monPort) {
        try {
            Socket socket = new Socket(brokerHost, Integer.parseInt(c2monPort.substring(1)));
            try {
                socket.close();
            } catch (IOException iOException) {
            }
        } catch (NumberFormatException | IOException e) {
            System.out.println("Connection to c2mon client '" + brokerHost + c2monPort + "' not available. Is the client host really running?");
            System.exit(1);
        }
    }

    protected String getAddressPortFromArg(String arg, String defaultPort) {
        arg = arg.trim().replaceAll("'", "");
        if (!arg.contains(":"))
            return arg.concat(":").concat(defaultPort);
        if (arg.endsWith(":0")) {
            String[] split = arg.split(":");
            return split[0].concat(":").concat(defaultPort);
        }
        return arg;
    }

    protected boolean checkHostConnection(String url) {
        try {
            String hostPort = url;
            String failoverPattern = "failover:";
            if (url.startsWith(failoverPattern))
                hostPort = url.substring(failoverPattern.length());
            String[] addressPort = hostPort.trim().split(":");
            if (addressPort.length > 2)
                return checkHostConnection(addressPort[1].trim().substring(2), Integer.parseInt(addressPort[2].trim()));
            return checkHostConnection(addressPort[0].trim(), Integer.parseInt(addressPort[1].trim()));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    protected boolean checkHostConnection(String hostAddress, int port) {
        try {
            Socket socket = new Socket(hostAddress, port);
            try {
                socket.close();
            } catch (IOException iOException) {
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
