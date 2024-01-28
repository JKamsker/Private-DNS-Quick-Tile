package com.jpwolfso.privdnsqt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.Arrays;

public class PrivateDnsConfigActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_dns_config);

        final SharedPreferences togglestates = getSharedPreferences("togglestates", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = togglestates.edit();

        final CheckBox checkoff = findViewById(R.id.check_off);
        final CheckBox checkauto = findViewById(R.id.check_auto);
        final CheckBox checkon = findViewById(R.id.check_on);

        final EditText texthostname = findViewById(R.id.text_hostname);

        final Button okbutton = findViewById(R.id.button_ok);

        if ((!hasPermission()) || togglestates.getBoolean("first_run", true) ){
            HelpMenu();
            editor.putBoolean("first_run", false).commit();
        }

        Boolean handled = false;
        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_ENABLE_DNS.equals(action)) {
                // Handle enable DNS action
                checkoff.setChecked(false);
                checkauto.setChecked(false);
                checkon.setChecked(true);
                Settings.Global.putString(getContentResolver(), "hostname", "hostname");
                handled = true;
                // String dnsprovider = Settings.Global.getString(getContentResolver(), "private_dns_specifier");
                // if (dnsprovider != null) {
                //     texthostname.setText(dnsprovider);
                //     Settings.Global.putString(getContentResolver(), "private_dns_mode", dnsprovider);
                // }
            } else if (ACTION_DISABLE_DNS.equals(action)) {
                // Handle disable DNS action
                checkoff.setChecked(true);
                checkauto.setChecked(false);
                checkon.setChecked(false);
                Settings.Global.putString(getContentResolver(), "private_dns_mode", "off");
                handled = true;
            } else if (ACTION_TOGGLE_DNS.equals(action)) {
                // Handle toggle DNS action
                String dnsmode = Settings.Global.getString(getContentResolver(), "private_dns_mode");
                Boolean isOn = !dnsmode.equalsIgnoreCase("off");
                if (isOn) {
                    checkoff.setChecked(false);
                    checkauto.setChecked(false);
                    checkon.setChecked(true);
                    Settings.Global.putString(getContentResolver(), "private_dns_mode", "hostname");
                } else {
                    checkoff.setChecked(true);
                    checkauto.setChecked(false);
                    checkon.setChecked(false);
                    Settings.Global.putString(getContentResolver(), "private_dns_mode", "off");
                }

                // if (dnsmode.equalsIgnoreCase("off")) {
                //     checkoff.setChecked(false);
                //     checkauto.setChecked(false);
                //     checkon.setChecked(true);
                //     Settings.Global.putString(getContentResolver(), "private_dns_mode", "hostname");
                //     handled = true;
                // } else if (dnsmode.equalsIgnoreCase("hostname")) {
                //     checkoff.setChecked(false);
                //     checkauto.setChecked(true);
                //     checkon.setChecked(false);
                //     Settings.Global.putString(getContentResolver(), "private_dns_mode", "opportunistic");
                //     handled = true;
                // } else if (dnsmode.equalsIgnoreCase("opportunistic")) {
                //     checkoff.setChecked(true);
                //     checkauto.setChecked(false);
                //     checkon.setChecked(false);
                //     Settings.Global.putString(getContentResolver(), "private_dns_mode", "off");
                //     handled = true;
                // }
            }
            setIntent(null);
        }

        if (!handled) {
            if (togglestates.getBoolean("toggle_off", true)) {
                checkoff.setChecked(true);
            }

            if (togglestates.getBoolean("toggle_auto", true)) {
                checkauto.setChecked(true);
            }

            if (togglestates.getBoolean("toggle_on", true)) {
                checkon.setChecked(true);
                texthostname.setEnabled(true);
            } else {
                texthostname.setEnabled(false);
            }
        }
       

        String dnsprovider = Settings.Global.getString(getContentResolver(), "private_dns_specifier");
        if (dnsprovider != null) {
            texthostname.setText(dnsprovider);
        }

        checkoff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (checkoff.isChecked()) {
                    editor.putBoolean("toggle_off", true);
                } else {
                    editor.putBoolean("toggle_off", false);
                }
            }
        });

        checkauto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (checkauto.isChecked()) {
                    editor.putBoolean("toggle_auto", true);
                } else {
                    editor.putBoolean("toggle_auto", false);
                }
            }
        });

        checkon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (checkon.isChecked()) {
                    editor.putBoolean("toggle_on", true);
                    texthostname.setEnabled(true);
                } else {
                    editor.putBoolean("toggle_on", false);
                    texthostname.setEnabled(false);
                }
            }
        });

        okbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasPermission()) {
                    if (checkon.isChecked()) {
                        if (texthostname.getText().toString().isEmpty()) {
                            Toast.makeText(PrivateDnsConfigActivity.this, R.string.toast_no_dns, Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            Settings.Global.putString(getContentResolver(), "private_dns_specifier", texthostname.getText().toString());
                        }
                    }
                    editor.commit();
                    Toast.makeText(PrivateDnsConfigActivity.this, R.string.toast_changes_saved, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(PrivateDnsConfigActivity.this, getString(R.string.toast_no_permission), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            updateShortcuts();
        }
    }

    private static final String ACTION_ENABLE_DNS = "com.jpwolfso.privdnsqt.ACTION_ENABLE_DNS";
    private static final String ACTION_DISABLE_DNS = "com.jpwolfso.privdnsqt.ACTION_DISABLE_DNS";
    private static final String ACTION_TOGGLE_DNS = "com.jpwolfso.privdnsqt.ACTION_TOGGLE_DNS";

    private void updateShortcuts() {
        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);

        ShortcutInfo enableDnsShortcut = new ShortcutInfo.Builder(this, "enable_dns")
                .setShortLabel("Enable DNS")
                .setLongLabel("Enable DNS")
                .setIcon(Icon.createWithResource(this, R.drawable.ic_dnson))
                .setIntent(new Intent(ACTION_ENABLE_DNS).setPackage(getPackageName()))
                .build();

        ShortcutInfo disableDnsShortcut = new ShortcutInfo.Builder(this, "disable_dns")
                .setShortLabel("Disable DNS")
                .setLongLabel("Disable DNS")
                .setIcon(Icon.createWithResource(this, R.drawable.ic_dnsoff))
                .setIntent(new Intent(ACTION_DISABLE_DNS).setPackage(getPackageName()))
                .build();

        ShortcutInfo toggleDnsShortcut = new ShortcutInfo.Builder(this, "toggle_dns")
                .setShortLabel("Toggle DNS")
                .setLongLabel("Toggle DNS")
                .setIcon(Icon.createWithResource(this, R.drawable.ic_dnsauto))
                .setIntent(new Intent(ACTION_TOGGLE_DNS).setPackage(getPackageName()))
                .build();

        shortcutManager.setDynamicShortcuts(Arrays.asList(enableDnsShortcut, disableDnsShortcut, toggleDnsShortcut));
    }

    public boolean hasPermission() {
        return checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != PackageManager.PERMISSION_DENIED;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_overflow, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_appinfo) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } else if (id == R.id.action_fdroid) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(getString(R.string.url_fdroid)));
            startActivity(intent);
        } else if (id == R.id.action_help) {
            HelpMenu();
        }
        return super.onMenuItemSelected(featureId, item);
    }

    public void HelpMenu() {
        LayoutInflater layoutInflater = LayoutInflater.from(PrivateDnsConfigActivity.this);
        View helpView = layoutInflater.inflate(R.layout.dialog_help, null);

        VideoView videoView = helpView.findViewById(R.id.videoView);
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.terminal));
        videoView.start();

        AlertDialog helpDialog = new AlertDialog
                .Builder(PrivateDnsConfigActivity.this)
                .setMessage(R.string.message_help)
                .setPositiveButton(android.R.string.ok, null)
                .setView(helpView)
                .create();
        helpDialog.show();

    }
}
