package com.acite.localtransfer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.acite.localtransfer.databinding.ActivityMainBinding;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setTitle("Transfer");
        binding.toolbar.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE,1);
            }
        }

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String action = intent.getAction();
        // 判断Intent是否是“分享”功能(Share Via)
        if (Intent.ACTION_SEND.equals(action))
        {
            if (extras.containsKey(Intent.EXTRA_STREAM))
            {
                try
                {
                    // 获取资源路径Uri

                    Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
                    Log.i("TAG", "uri:" + uri.toString());
                    //解析Uri资源
                    //ContentResolver cr = getContentResolver();
                    //InputStream is = cr.openInputStream(uri);
                    // Get binary bytes for encode
                    String path=getRealFilePath(this,uri);
                    try {
                        Conversation newConv=new Conversation();
                        newConv.TransFile(uri, path.substring(path.lastIndexOf("/") + 1, path.length()), this, new Runnable() {
                            @Override
                            public void run() {

                            }
                        });
                    }
                    catch (Exception ex){
                        Toast.makeText(this, "Error:" + ex.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                    Toast toast = Toast.makeText(this, "Begin transfer file:" + path.substring(path.lastIndexOf("/") + 1, path.length()),
                            Toast.LENGTH_SHORT);
                    toast.show();

                    return;
                }
                catch (Exception e)
                {
                    Log.e(this.getClass().getName(), e.toString());
                }

            }
            else if (extras.containsKey(Intent.EXTRA_TEXT))
            {
                return;
            }
        }
    }
    public static String getRealFilePath( final Context context, final Uri uri ) {
        if ( null == uri ) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if ( scheme == null )
            data = uri.getPath();
        else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
            data = uri.getPath();
        } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
            Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
            if ( null != cursor ) {
                if ( cursor.moveToFirst() ) {
                    int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
                    if ( index > -1 ) {
                        data = cursor.getString( index );
                    }
                }
                cursor.close();
            }
        }
        return data;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            exit(this);
        }

        return super.onOptionsItemSelected(item);
    }
    public static void exit(Context context){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        //让Activity的生命周期进入后台，否则在某些手机上即使sendSignal 3和9了，还是由于Activity的生命周期导致进程退出不了。除非调用了Activity.finish()
        context.startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
        //System.runFinalizersOnExit(true);
        System.exit(0);
    }


}