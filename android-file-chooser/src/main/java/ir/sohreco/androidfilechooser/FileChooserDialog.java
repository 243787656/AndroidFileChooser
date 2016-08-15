package ir.sohreco.androidfilechooser;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileChooserDialog extends AppCompatDialogFragment implements ItemHolder.OnItemClickListener, View.OnClickListener {
    private final static String KEY_CHOOSER_TYPE = "chooserType";
    private final static String KEY_CHOOSER_LISTENER = "chooserListener";
    private final static String KEY_TITLE = "title";
    private final static String KEY_FILE_FORMATS = "fileFormats";
    private final static String KEY_FILE_ICON_ID = "fileIconId";
    private final static String KEY_DIRECTORY_ICON_ID = "directoryIconId";
    private final static String KEY_PREVIOUS_DIRECTORY_BUTTON_ICON_ID = "previousDirectoryButtonIconId";

    private Button btnPrevDir, btnSelectDir;
    private RecyclerView rvItems;
    private TextView tvCurrentDir;
    private ChooserType chooserType;
    private ChooserListener chooserListener;
    private ItemsAdapter itemsAdapter;
    private String[] fileFormats;
    private String currentDirPath, title;
    @DrawableRes
    private int directoryIconId, fileIconId, previousDirectoryButtonIconId;

    public interface ChooserListener extends Serializable {
        /**
         * This method gets called when user selects a file or a directory depending on the chooser type.
         *
         * @param path path of the selected file or directory.
         */
        void onSelect(String path);
    }

    public enum ChooserType {
        FILE_CHOOSER,
        DIRECTORY_CHOOSER
    }

    public static class Builder {
        // Required parameters
        private ChooserType chooserType;
        private ChooserListener chooserListener;

        // Optional parameters
        private String[] fileFormats;
        private String title;
        @DrawableRes
        private int fileIconId = R.drawable.ic_file;
        @DrawableRes
        private int directoryIconId = R.drawable.ic_directory;
        @DrawableRes
        private int previousDirectoryButtonIcon = R.drawable.ic_prev_dir;

        /**
         * Creates a builder for a FileChooser fragment.
         *
         * @param chooserType You can choose to create either a FileChooser or a DirectoryChooser
         */
        public Builder(ChooserType chooserType, ChooserListener chooserListener) {
            this.chooserType = chooserType;
            this.chooserListener = chooserListener;
        }

        /**
         * Set the title of the File Chooser.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * Set file formats which are going to be shown by the File Chooser
         * All types of files will be shown if you don't set it.
         *
         * @param fileFormats A string array of file formats
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setFileFormats(String[] fileFormats) {
            this.fileFormats = fileFormats;
            return this;
        }

        /**
         * Set the icon for files in the File Chooser's list
         * Default icon will be used if you don't set it.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setFileIcon(@DrawableRes int fileIconId) {
            this.fileIconId = fileIconId;
            return this;
        }

        /**
         * Set the icon for directories in the File Chooser's list
         * Default icon will be used if you don't set it.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setDirectoryIcon(@DrawableRes int directoryIconId) {
            this.directoryIconId = directoryIconId;
            return this;
        }

        /**
         * Set the icon for the button that is going to be used to go to the parent of selected directory
         * Default icon will be used if you don't set it.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        public Builder setPreviousDirectoryButtonIcon(@DrawableRes int previousDirectoryButtonIcon) {
            this.previousDirectoryButtonIcon = previousDirectoryButtonIcon;
            return this;
        }

        /**
         * Returns an instance of FileChooserDialog with the given configurations.
         *
         * @throws ExternalStorageNotAvailableException If there is no external storage available on user's device
         */
        public FileChooserDialog build() throws ExternalStorageNotAvailableException {
            String externalStorageState = Environment.getExternalStorageState();
            boolean externalStorageAvailable = externalStorageState.equals(Environment.MEDIA_MOUNTED)
                    || externalStorageState.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
            if (!externalStorageAvailable) {
                throw new ExternalStorageNotAvailableException();
            }

            FileChooserDialog fragment = new FileChooserDialog();
            Bundle args = new Bundle();
            args.putSerializable(KEY_CHOOSER_TYPE, chooserType);
            args.putSerializable(KEY_CHOOSER_LISTENER, chooserListener);
            args.putString(KEY_TITLE, title);
            args.putStringArray(KEY_FILE_FORMATS, fileFormats);
            args.putInt(KEY_FILE_ICON_ID, fileIconId);
            args.putInt(KEY_DIRECTORY_ICON_ID, directoryIconId);
            args.putInt(KEY_PREVIOUS_DIRECTORY_BUTTON_ICON_ID, previousDirectoryButtonIcon);
            fragment.setArguments(args);

            return fragment;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getGivenArguments();
        if (title == null) {
            // Remove dialog's title
            // Since setting the style after the fragment is created doesn't have any effect
            // so we have to decide about dialog's title here.
            setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chooser, container, false);
        findViews(view);
        setListeners();

        getDialog().setTitle(title);
        if (chooserType == ChooserType.DIRECTORY_CHOOSER) {
            btnSelectDir.setVisibility(View.VISIBLE);
        }
        btnPrevDir.setBackgroundResource(previousDirectoryButtonIconId);

        itemsAdapter = new ItemsAdapter(this);
        rvItems.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvItems.setAdapter(itemsAdapter);

        loadItems(Environment.getExternalStorageDirectory().getPath());
        return view;
    }

    @Override
    public void onItemClick(Item item) {
        if (item.isDirectory()) {
            loadItems(item.getPath());
        } else {
            chooserListener.onSelect(item.getPath());
            dismiss();
        }
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.previous_dir_imagebutton) {
            String parent = new File(currentDirPath).getParent();
            if (parent != null) {
                loadItems(parent);
            }
        } else if (i == R.id.select_dir_button) {
            chooserListener.onSelect(currentDirPath);
            dismiss();
        }
    }

    private void loadItems(String path) {
        currentDirPath = path;

        String currentDir = path.substring(path.lastIndexOf("/") + 1);
        tvCurrentDir.setText(currentDir);

        File[] files = new File(path).listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.canRead()) {
                    if (chooserType == ChooserType.FILE_CHOOSER && file.isFile()) {
                        if (fileFormats != null) {
                            for (String fileFormat : fileFormats) {
                                if (file.getName().endsWith(fileFormat)) {
                                    return true;
                                }
                            }
                            return false;
                        }
                        return true;
                    }
                    return file.isDirectory();
                }
                return false;
            }
        });

        List<Item> items = new ArrayList<>();
        for (File f : files) {
            int drawableId = f.isFile() ? fileIconId : directoryIconId;
            Drawable drawable = ContextCompat.getDrawable(getActivity().getApplicationContext(), drawableId);
            items.add(new Item(f.getPath(), drawable));
        }
        Collections.sort(items);
        itemsAdapter.setItems(items);
    }

    private void getGivenArguments() {
        Bundle args = getArguments();
        chooserType = (ChooserType) args.getSerializable(KEY_CHOOSER_TYPE);
        chooserListener = (ChooserListener) args.getSerializable(KEY_CHOOSER_LISTENER);
        title = args.getString(KEY_TITLE);
        fileFormats = args.getStringArray(KEY_FILE_FORMATS);
        fileIconId = args.getInt(KEY_FILE_ICON_ID);
        directoryIconId = args.getInt(KEY_DIRECTORY_ICON_ID);
        previousDirectoryButtonIconId = args.getInt(KEY_PREVIOUS_DIRECTORY_BUTTON_ICON_ID);
    }

    private void setListeners() {
        btnPrevDir.setOnClickListener(this);
        btnSelectDir.setOnClickListener(this);
    }

    private void findViews(View v) {
        rvItems = (RecyclerView) v.findViewById(R.id.items_recyclerview);
        btnPrevDir = (Button) v.findViewById(R.id.previous_dir_imagebutton);
        btnSelectDir = (Button) v.findViewById(R.id.select_dir_button);
        tvCurrentDir = (TextView) v.findViewById(R.id.current_dir_textview);
    }
}