package stream.recentimagesapp;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import stream.custombutton.CustomButton;
import stream.pickphotoview.PickPhotoView;
import stream.recentimages.ImageAdapter;
import stream.recentimages.RecentImages;
import com.bumptech.glide.Glide;
import stream.jess.ui.TwoWayAdapterView;
import stream.jess.ui.TwoWayGridView;

public class MainActivity extends AppCompatActivity {

    private static String[] permissionList = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    private static final int CAPTURE_IMAGE = 0;
    private static final int SELECT_PHOTO = 1;

    private Uri imageUri;
    private ImageView mImage;
    private ContentResolver cr;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();

        //Permissions need to be granted at runtime on Marshmallow
        if (Build.VERSION.SDK_INT >= 21) {
            CheckPermissions();
        }

        cr = this.getContentResolver();
        mImage = findViewById(R.id.imageView);

        //Initialize Recent Images Dialogue Popup.
        final View bottomSheet = getLayoutInflater().inflate(R.layout.bottom_sheet, null);
        final Dialog mBottomSheetDialog = new Dialog(this, R.style.MaterialDialogSheet);
        mBottomSheetDialog.setContentView(bottomSheet);
        mBottomSheetDialog.setCancelable(true);
        mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mBottomSheetDialog.getWindow().setGravity(Gravity.BOTTOM);

        //Initialize Recent Images Menu Actions.
        LinearLayout layoutCamera = bottomSheet.findViewById(R.id.btn_camera);
        LinearLayout layoutGallery = bottomSheet.findViewById(R.id.btn_gallery);
        layoutCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAPTURE_IMAGE);
                mBottomSheetDialog.dismiss();
            }
        });
        layoutGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
                showImageGallery();
            }
        });

        CustomButton btn1 = findViewById(R.id.btn1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RecentImages ri = new RecentImages();
                ri.setHeight(100);
                ri.setWidth(100);
                ImageAdapter adapter = ri.getAdapter(MainActivity.this);

                TwoWayGridView gridview = bottomSheet.findViewById(R.id.gridview);
                gridview.getLayoutParams().height = Units.dpToPx(mContext, 100);
                gridview.setNumRows(1);
                gridview.setAdapter(adapter);
                gridview.setOnItemClickListener(new TwoWayAdapterView.OnItemClickListener() {
                    public void onItemClick(TwoWayAdapterView parent, View v, int position, long id) {
                        imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                        Glide.with(mContext).load(imageUri).into(mImage);
                        mBottomSheetDialog.dismiss();
                    }
                });

                mBottomSheetDialog.show();
            }
        });

        CustomButton btn2 = findViewById(R.id.btn2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RecentImages ri = new RecentImages();
                ri.setHeight(100);
                ri.setWidth(100);
                ImageAdapter adapter = ri.getAdapter(MainActivity.this);

                TwoWayGridView gridview = bottomSheet.findViewById(R.id.gridview);
                gridview.getLayoutParams().height = Units.dpToPx(mContext, 200);
                gridview.setNumRows(2);
                gridview.setAdapter(adapter);
                gridview.setOnItemClickListener(new TwoWayAdapterView.OnItemClickListener() {
                    public void onItemClick(TwoWayAdapterView parent, View v, int position, long id) {
                        imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                        Glide.with(mContext).load(imageUri).into(mImage);
                        mBottomSheetDialog.dismiss();
                    }
                });

                mBottomSheetDialog.show();
            }
        });
    }

    private void showImageGallery() {
        new PickPhotoView.Builder(MainActivity.this)
                .setPickPhotoSize(1)
                .setShowCamera(false)
                .setSpanCount(3)
                .setLightStatusBar(true)
                .setStatusBarColor('#' + Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.white)))
                .setToolbarColor('#' + Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.white)))
                .setToolbarIconColor('#' + Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)))
                .setSelectIconColor('#' + Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryLight)))
                .setClickSelectable(true)
                .start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CAPTURE_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    imageUri = data.getData();
                }
                break;
            case SELECT_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    imageUri = data.getData();
                }
                break;
        }
        if (imageUri != null) {
            Log.d("ImageURI", String.valueOf(imageUri));
            Glide.with(mContext).load(imageUri).into(mImage);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Thanks!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    //If user denies Storage Permission, explain why permission is needed and prompt again.
                    Toast.makeText(this, "Storage access is needed to display images.",
                            Toast.LENGTH_SHORT).show();
                    CheckPermissions();
                }
                break;
            default:
                break;
        }
    }

    public void CheckPermissions()
    {
        if (!IsPermissionsEnabled(mContext, permissionList))
        {
            ActivityCompat.requestPermissions(this, permissionList, 1);
        }
    }

    public boolean IsPermissionEnabled(Context context, String permission)
    {
        if (Build.VERSION.SDK_INT >= 23) {
            return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
        else
        {
            return true;
        }
    }

    public boolean IsPermissionsEnabled(Context context, String[] permissionList)
    {
        for (String permission : permissionList)
        {
            if (!IsPermissionEnabled(context, permission))
            {
                return false;
            }
        }

        return true;
    }
}
