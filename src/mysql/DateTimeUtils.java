package mysql;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUtils {

	public static String formatDateToStringDB(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(date);
	}
}
