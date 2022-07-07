package cu.axel.litebrowser;
import android.content.Context;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;

public class TabsAdapter extends ArrayAdapter<WebView> {
    private Context context;
    public TabsAdapter(Context context, ArrayList<WebView> tabs){
        super(context,R.layout.tab_row, tabs);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.tab_row, null);
            
        TextView tabTitleTv = convertView.findViewById(R.id.tab_row_title);
        TextView tabUrlTv = convertView.findViewById(R.id.tab_row_url);
        
        WebView wv = getItem(position);
        
        tabTitleTv.setText(wv.getTitle());
        tabUrlTv.setText(wv.getUrl());
        
        return convertView;
    }
    
    
}
