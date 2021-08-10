package com.sin.showdatahh;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class DeleteActivity extends AppCompatActivity {

    private Button button1,button2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);

        button1=findViewById(R.id.button1);
        button2=findViewById(R.id.button2);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*String  path= Environment.getExternalStorageDirectory().getAbsolutePath()
                        + File.separator + "171DataSave" + File.separator;*/
                String path=DeleteActivity.this.getExternalFilesDir(null).getPath()+ File.separator + "171DataSave" + File.separator;

                boolean isSuccess=FileUtils.deleteFile(path);
                if(isSuccess) {
                    Toast.makeText(DeleteActivity.this,"删除成功！",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(DeleteActivity.this,"删除失败！",Toast.LENGTH_SHORT).show();
                }
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}