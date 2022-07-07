package cu.axel.litebrowser;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.PopupMenu;
import androidx.core.os.EnvironmentCompat;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import android.util.Log;

public class Utils {

    public static boolean isUrl(String str) { 
        if (!str.contains(".") || str.substring(str.indexOf(".")).length() <= 2 || str.contains(" ")) { 
            return false; 
        } 
        return true; 
    }

    public static String getUrl(String query, int handler) {
        if (Utils.isUrl(query)) {
            return query;
        } else {
            query = URLEncoder.encode(query);

            switch (handler) {
                case 0:
                    return "https://www.google.com/search?q=" + query;
                case 1:
                    return "https://lite.duckduckgo.com/lite?q=" + query;
                case 2:
                    if (Locale.getDefault().getLanguage().equals("es"))
                        return "https://es.wikipedia.org/wiki/Special:Search?search=" + query;
                    else
                        return "https://en.wikipedia.org/wiki/Special:Search?search=" + query;
                case 3:
                    return "https://consolegames.down10.software/search?name=" + query;
                case 4:
                    return "https://search.f-droid.org/?q=" + query;
                case 5:
                    return "https://www.zophar.net/music/search?search=" + query + "&search_consoleid=0";
            }
        }
        return "";
	}
    
    public static String getBaseUrl(String str) {
        return str.substring(0, str.indexOf("/", str.indexOf("/") + 2));
    }

    public static String getMimeType(String str) {
        String fileExtensionFromUrl = MimeTypeMap.getFileExtensionFromUrl(str);
        if (fileExtensionFromUrl != null) {
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtensionFromUrl);
        }
        return fileExtensionFromUrl;
    }

    public static void setForceShowIcon(PopupMenu popupMenu) {
        try {
            Field[] declaredFields = popupMenu.getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object obj = field.get(popupMenu);
                    Class.forName(obj.getClass().getName()).getMethod("setForceShowIcon", Boolean.TYPE).invoke(obj, new Boolean(true));
                    return;
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }
    
    public static String strip(String str) {
        return str.replace("/", "").replace(":", "").replace("|", "").replace("\\", "");
    }

    public static String getReadableFileSize(long j) {
        if (j == ((long) -1)) {
            return EnvironmentCompat.MEDIA_UNKNOWN;
        }
        if (j <= ((long) 0)) {
            return "0";
        }
        String[] strArr = {"B", "kB", "MB", "GB", "TB"};
        int log10 = (int) (Math.log10((double) j) / Math.log10((double) 1024));
        return new StringBuffer().append(new StringBuffer().append(new DecimalFormat("#,##0.#").format(((double) j) / Math.pow((double) 1024, (double) log10))).append(" ").toString()).append(strArr[log10]).toString();
    }

    public static String getCap(String str) {
        return str.substring(0, 1);
    }

    public static String getDateTime(long j) {
        return new SimpleDateFormat("MMM dd/yyyy hh:mm a", Locale.getDefault()).format(new Long(j));
    }


    public static boolean isLocalUrl(String str) {
        return str.startsWith("file://") || str.startsWith("content://");
    }

    public static String getFileNameFromUrl(String str) {
        return URLUtil.guessFileName(str, null, MimeTypeMap.getFileExtensionFromUrl(str));
    }

    public static String getFileNameFromHeader(String str) {
        String str2;
        if (str == null) {
            return null;
        }
        int lastIndexOf = str.toLowerCase().lastIndexOf("filename=");
        if (lastIndexOf >= 0) {
            str2 = str.substring(lastIndexOf + 9);
            int lastIndexOf2 = str2.lastIndexOf(";");
            if (lastIndexOf2 > 0) {
                str2 = str2.substring(0, lastIndexOf2 - 1);
            }
        } else {
            str2 = null;
        }
        if (str2 != null) {
            return str2.replaceAll("\"", "");
        }
        return str2;
    }

}
