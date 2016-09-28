package ext;

import models.Constants;
import play.mvc.Router;
import play.templates.JavaExtensions;

import java.util.*;

public class PaginationExtensions extends JavaExtensions {

    public static String buildUrl(String url, Map<String, String[]> params, int page) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("page", page);
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String[]> param : params.entrySet()) {
                map.put(param.getKey(), Arrays.asList(param.getValue()));
            }
        }
        return Router.reverse(url, map).url;
    }

    public static List<Integer> buildPossiblePages(int page, int results) {
        int totalPages = Math.max(1, (int) Math.ceil((double) results / Constants.Utils.MAX_RESULTS_PAGE));
        List<Integer> pages = new ArrayList<Integer>();
        if (page == 1) {
            for (int i = page; i <= Math.min(page + 2, totalPages); i++) {
                pages.add(i);
            }
        } else if (page == totalPages) {
            for (int i = Math.max(totalPages - 2, 1); i <= totalPages; i++) {
                pages.add(i);
            }
        } else {
            for (int i = page - 1; i <= Math.min(page + 1, totalPages); i++) {
                pages.add(i);
            }
        }
        if (pages.size() == 3 && pages.get(2) < totalPages) {
            pages.add(totalPages);
        }
        return pages;
    }

}
