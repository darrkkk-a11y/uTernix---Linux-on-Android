package lab.deblauncher;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private TextView status;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(48, 72, 48, 48);
        TextView title = new TextView(this);
        title.setText("DEB Launcher");
        title.setTextSize(28);
        root.addView(title);
        status = new TextView(this);
        status.setTextSize(16);
        status.setPadding(0, 32, 0, 32);
        status.setText("Выберите Linux-пакет .deb.\n\nЗапуск приложений пока не поддерживается: Linux-окружение, графический сервер и эмулятор x86-64 ещё не встроены.");
        root.addView(status);
        Button choose = new Button(this);
        choose.setText("Выбрать DEB");
        choose.setOnClickListener(new View.OnClickListener() { public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, 1);
        }});
        root.addView(choose);
        setContentView(root);
    }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request != 1 || result != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        String name = "Выбранный файл";
        try (Cursor cursor = getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst() && !cursor.isNull(0)) name = cursor.getString(0);
        } catch (Exception ignored) { }
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            byte[] magic = new byte[8];
            int offset = 0;
            while (offset < magic.length) {
                int read = in == null ? -1 : in.read(magic, offset, magic.length - offset);
                if (read < 0) break;
                offset += read;
            }
            boolean ar = offset == 8 && "!<arch>\n".equals(new String(magic, StandardCharsets.US_ASCII));
            status.setText(name + (ar ? "\n\nФормат ar обнаружен; это предварительная проверка, не подтверждение корректности DEB." : "\n\nФайл не похож на DEB (нет заголовка ar).") + "\n\nЗапуск пока не поддерживается. Файл не устанавливался и не запускался.");
        } catch (Exception e) {
            status.setText("Не удалось прочитать файл: " + e.getClass().getSimpleName());
        }
    }
}
