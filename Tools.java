package com.ferreiraz.lib;

import android.os.Environment;
import android.util.Log;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Tools {

    public static void AssyncDownloads() {
/*        new Thread(new Runnable() {
            @Override
            public void run() {
                EVPContract.Aulas aula = EVPContract.GetAulaToDownload();
                if(aula.status == 1) {
                    if(aula.link_pdf != "") {
                        String[] stringArray = aula.link_pdf.split("/");
                        File file = new File(LoadingActivity.DOWNLOAD_DIR, stringArray[stringArray.length - 1]);
                        DownloadFile(file, aula.link_pdf, aula.name);
                    }
                    if(aula.link_mp4 != "") {
                        String[] stringArray = aula.link_mp4.split("/");
                        File file = new File(LoadingActivity.DOWNLOAD_DIR, stringArray[stringArray.length - 1]);
                        DownloadFile(file, aula.link_mp4, aula.name);
                    }
                    if(aula.link_flv != "") {
                        String[] stringArray = aula.link_flv.split("/");
                        File file = new File(LoadingActivity.DOWNLOAD_DIR, stringArray[stringArray.length - 1]);
                        DownloadFile(file, aula.link_flv, aula.name);
                    }
                }
            }
        }).start();*/
    }

    static Boolean isDownloading = false;
    static HashMap<String, DownloadDataInfo> StatusList;

    public static void LogException(Exception e) {
        LogDetails(e.getLocalizedMessage().concat("\r\n\t").concat(e.getMessage()));
    }

    public static void LogDetails(String string) {

        Log.e("FZ", string.concat("\r\n======================================================================"));
    }

    public static String ExpansionPath(String packName) {

        return Environment.getExternalStorageDirectory().getAbsolutePath()
                .concat("/Android/obb/").concat(StaticPackageName())
                .concat("/").concat(packName);
    }

    public static String MainExpansionPath() {
        return ExpansionPath(MainExpansionName());
    }

    public static String MainExpansionName() {
        return "main.1.".concat(StaticPackageName()).concat(".obb");
    }

    public static String StaticPackageName() {
        return Tools.class.getPackage().getName();
    }

    public static Boolean DownloadFile(File file, String url, String descriptor) {
        if(!file.exists()) {
            OutputStream fileOutput;
            try {
                LogDetails("Iniciando Download de " + url);
                fileOutput = new FileOutputStream(file);
                byte[] bytes = DownloadData(url, descriptor).toByteArray();
                if(bytes == null)
                    throw new Exception("Erro de Download");
                fileOutput.write(bytes);
                fileOutput.flush();
                fileOutput.close();
                LogDetails("Concluido Download de " + url);
            } catch(Exception e) {
                LogException(e);
            }
        }
        return file.exists();
    }

    public static Boolean DownloadFile(File file, String urlString) {
        return DownloadFile(file, urlString, urlString);
    }

    public static String GZIPStreamToString(GZIPInputStream gzipInputStream) {
        String returnString = "";

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            for (int value = 0; value != -1;) {
                value = gzipInputStream.read();
                if (value != -1) {
                    byteArrayOutputStream.write(value);
                }
            }
            gzipInputStream.close();
            byteArrayOutputStream.close();
            returnString = new String(byteArrayOutputStream.toByteArray(), "UTF-8");
        } catch (Exception e) {

        }
        return returnString;
    }

    static class DownloadDataInfo {
        String name = "";
        int bytesLength = 0;
        int bytesDone = 0;
        int bytesPercent = 0;

        public DownloadDataInfo(String name) {
            this.name = name;
        }


        public String getName() {
            return this.name;
        }

        public void setBytesLength(int bytesLength) {
            this.bytesLength = bytesLength;
        }

        public void setBytesDone(int bytesDone) {
            this.bytesDone = bytesDone;

            if(bytesDone > 0) {
                bytesPercent = (int) (bytesDone * 100 / bytesLength);
            }
        }

        public int getPercent() {
            return bytesPercent;
        }
    }
    public static ByteArrayOutputStream DownloadData(String uri) {
        String[] stringArray = uri.split("/");
        return DownloadData(uri, stringArray[stringArray.length - 1]);
    }

    public static ByteArrayOutputStream DownloadData(String uri, String descriptor) {
        if(StatusList == null)
            StatusList = new HashMap<String, DownloadDataInfo>();
        URL url;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            DownloadDataInfo downloadDataInfo = new DownloadDataInfo(descriptor);
            StatusList.put(uri, downloadDataInfo);
            url = new URL (uri);
            URLConnection urlConnection = url.openConnection();
            urlConnection.connect();
            StatusList.get(uri).setBytesLength(urlConnection.getContentLength());
            InputStream inputStream = urlConnection.getInputStream();
            try {
                try {
                    byte[] byteArray = new byte[2048];
                    int bytesRead = 0;

                    while ((bytesRead = inputStream.read(byteArray, 0, byteArray.length)) >= 0) {
                        byteArrayOutputStream.write(byteArray, 0, bytesRead);
                        StatusList.get(uri).setBytesDone(byteArrayOutputStream.size());
                    }
                } finally {
                    if(byteArrayOutputStream != null)
                        byteArrayOutputStream.close();
                }
            } finally {
                if(inputStream != null)
                    inputStream.close();
            }
        } catch(Exception e) {
            LogException(e);
        } finally {
            if(StatusList.containsKey(uri))
                StatusList.remove(uri);
        }
        return byteArrayOutputStream;
    }

    public static boolean unpackZip(String path, String zipname)
    {
        InputStream inputStream;
        ZipInputStream zipInputStream;
        try
        {
            LogDetails("Iniciando Unzip de " + zipname);
            String filename;

            inputStream = new FileInputStream(zipname);
            zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream));
            ZipEntry zipEntry;
            byte[] buffer = new byte[1024];
            int count;

            if(path.substring(path.length() - 1) != "/")
                path = path.concat("/");

            while ((zipEntry = zipInputStream.getNextEntry()) != null)
            {
                filename = zipEntry.getName();
                if (zipEntry.isDirectory()) {
                    File fmd = new File(path + filename);
                    fmd.mkdirs();
                    continue;
                }

                File previous = new File(path + filename);
                if(previous.exists())
                    previous.delete();

                FileOutputStream fileOutputStream = new FileOutputStream(path + filename);
                while ((count = zipInputStream.read(buffer)) != -1)
                {
                    fileOutputStream.write(buffer, 0, count);
                }

                fileOutputStream.close();
                zipInputStream.closeEntry();
            }

            zipInputStream.close();
            LogDetails("Concluido Unzip de " + zipname);
        }
        catch(IOException e)
        {
            LogException(e);
            return false;
        }

        return true;
    }

    public static HashMap<String, ByteArrayOutputStream> UnpackToStreams(ByteArrayInputStream byteArrayInputStream) {
        ZipInputStream zipInputStream;
        HashMap<String, ByteArrayOutputStream> returnInformation = new HashMap<String, ByteArrayOutputStream>();
        try
        {
            LogDetails("Iniciando Unzip");
            zipInputStream = new ZipInputStream(byteArrayInputStream);
            ZipEntry zipEntry;
            byte[] buffer = new byte[1024];
            int count;

            while ((zipEntry = zipInputStream.getNextEntry()) != null)
            {
                String entryName = zipEntry.getName();
                if(!entryName.contains("fzn"))
                    continue;

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                while ((count = zipInputStream.read(buffer)) != -1)
                {
                    byteArrayOutputStream.write(buffer, 0, count);
                }

                byteArrayOutputStream.close();

                returnInformation.put(entryName, byteArrayOutputStream);

                zipInputStream.closeEntry();
            }

            zipInputStream.close();
            LogDetails("Concluido Unzip");
        } catch(IOException e) {
            LogException(e);
        }
        return returnInformation;
    }

    public static String StreamToString(InputStream inputStream) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String readedLine = "";

            while ((readedLine = bufferedReader.readLine()) != null) {
                stringBuilder.append(readedLine);
            }
        } catch (Exception e) {
            LogException(e);
        }

        return stringBuilder.toString();
    }

    public static String FileToString(File file) {
        String string = "";
        try {
            LogDetails("Iniciando Leitura de " + file.getAbsolutePath());
            FileInputStream fileInputStream = new FileInputStream(file);
            string = StreamToString(fileInputStream);
            fileInputStream.close();
            LogDetails("Concluida Leitura de " + file.getAbsoluteFile());
        } catch (Exception e) {
            LogException(e);
        }
        return string;
    }
}
