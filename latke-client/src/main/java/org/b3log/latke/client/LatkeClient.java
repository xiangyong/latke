/*
 * Copyright (c) 2009, 2010, 2011, 2012, B3log Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b3log.latke.client;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

/**
 * Latke client.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Apr 11, 2012
 */
public final class LatkeClient {

    /**
     * Client version.
     */
    private static final String VERSION = "0.1.0";
    /**
     * Gets repository names.
     */
    private static final String GET_REPOSITORY_NAMES = "/latke/remote/repository/names";
    /**
     * Sets repositories writable.
     */
    private static final String SET_REPOSITORIES_WRITABLE = "/latke/remote/repositories/writable";
    /**
     * Server address, starts with http://.
     */
    private static String serverAddress = "";
    /**
     * Backup directory.
     */
    private static File backupDir;
    /**
     * User name.
     */
    private static String userName = "";
    /**
     * Password.
     */
    private static String password = "";
    /**
     * Verbose.
     */
    private static boolean verbose;

    /**
     * Main entry.
     * 
     * @param args the specified command line arguments
     */
    public static void main(final String[] args) {
        final Options options = getOptions();

        final CommandLineParser parser = new PosixParser();

        try {
            final CommandLine cmd = parser.parse(options, args);

            if (!cmd.hasOption("s")) {
                System.out.println("Expected [server]");
                printHelp(options);
                return;
            }

            serverAddress = cmd.getOptionValue("server");

            if (!cmd.hasOption("backup_dir")) {
                System.out.println("Expected [backup_dir]");
                printHelp(options);
                return;
            }

            backupDir = new File(cmd.getOptionValue("backup_dir"));
            if (!backupDir.exists()) {
                backupDir.mkdir();
            }

            if (!cmd.hasOption("user_name")) {
                System.out.println("Expected [user_name]");
                printHelp(options);
                return;
            }

            userName = cmd.getOptionValue("user_name");

            if (!cmd.hasOption("password")) {
                System.out.println("Expected [password]");
                printHelp(options);
                return;

            }

            if (cmd.hasOption("verbose")) {
                verbose = true;
            }

            password = cmd.getOptionValue("password");

            if (verbose) {
                System.out.println("Requesting server[" + serverAddress + "]");
            }

            final HttpClient httpClient = new DefaultHttpClient();

            final List<NameValuePair> qparams = new ArrayList<NameValuePair>();
            qparams.add(new BasicNameValuePair("userName", userName));
            qparams.add(new BasicNameValuePair("password", password));

            if (cmd.hasOption("rn")) {
                try {
                    final URI uri = URIUtils.createURI("http", serverAddress, -1, GET_REPOSITORY_NAMES,
                                                       URLEncodedUtils.format(qparams, "UTF-8"), null);
                    final HttpGet request = new HttpGet();
                    request.setURI(uri);

                    if (verbose) {
                        System.out.println("Getting repository names[" + GET_REPOSITORY_NAMES + "]");
                    }

                    final HttpResponse httpResponse = httpClient.execute(request);
                    final InputStream contentStream = httpResponse.getEntity().getContent();
                    final String content = IOUtils.toString(contentStream).trim();

                    if (verbose) {
                        printResponse(content);
                    }


                } catch (final Exception e) {
                    System.err.println("Requests server error: " + e.getMessage());
                }
            }

            if (cmd.hasOption("writable")) {
                try {
                    final String writable = cmd.getOptionValue("writable");
                    qparams.add(new BasicNameValuePair("writable", writable));
                    final URI uri = URIUtils.createURI("http", serverAddress, -1, SET_REPOSITORIES_WRITABLE,
                                                       URLEncodedUtils.format(qparams, "UTF-8"), null);
                    final HttpPut request = new HttpPut();
                    request.setURI(uri);

                    if (verbose) {
                        System.out.println("Setting repository writable[" + writable + "]");
                    }

                    final HttpResponse httpResponse = httpClient.execute(request);
                    final InputStream contentStream = httpResponse.getEntity().getContent();
                    final String content = IOUtils.toString(contentStream).trim();

                    if (verbose) {
                        printResponse(content);
                    }


                } catch (final Exception e) {
                    System.err.println("Requests server error: " + e.getMessage());
                }
            }


        } catch (final ParseException e) {
            System.err.println("Parsing args failed, caused by: " + e.getMessage());
            printHelp(options);
        }
    }

    /**
     * Prints the specified content as response.
     * 
     * @param content the specified content
     */
    private static void printResponse(final String content) {
        System.out.println("Response[");
        System.out.println("    " + content);
        System.out.println("]");
    }

    /**
     * Prints help with the specified options.
     * 
     * @param options the specified options
     */
    private static void printHelp(final Options options) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("latke-client", options);
    }

    /**
     * Gets options.
     * 
     * @return options
     */
    private static Options getOptions() {
        final Options ret = new Options();

        final Option srvAddressOpt = new Option("server", "s", true, "Uses SERVER");
        srvAddressOpt.setArgName("SERVER");
        ret.addOption(srvAddressOpt);

        final Option userNameOpt = new Option("user_name", true, "Uses USER_NAME");
        userNameOpt.setArgName("USER_NAME");
        ret.addOption(userNameOpt);

        final Option passwordOpt = new Option("password", true, "Uses PASSWORD");
        passwordOpt.setArgName("PASSWORD");
        ret.addOption(passwordOpt);

        final Option backupDirOpt = new Option("backup_dir", true, "Uses BACKUP_DIR");
        backupDirOpt.setArgName("BACKUP_DIR");
        ret.addOption(backupDirOpt);

        final Option writableOpt = new Option("writable", true, "Uses WRITABLE");
        writableOpt.setArgName("WRITABLE");
        ret.addOption(writableOpt);

        final Option repositoryNamesOpt = new Option("repository_names", "rn", false, "Prints repositories names");
        ret.addOption(repositoryNamesOpt);

        final Option verboseOpt = new Option("verbose", false, "Runs with extra verbose");
        ret.addOption(verboseOpt);

        final Option helpOpt = new Option("help", "h", false, "Prints this message");
        ret.addOption(helpOpt);

        final Option versionOpt = new Option("version", "v", false, "Prints the cient version information and exit");
        ret.addOption(versionOpt);

        return ret;
    }

    /**
     * Private constructor.
     */
    private LatkeClient() {
    }
}
