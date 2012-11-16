/*
 * Copyright (C) 2012 Timo Vesalainen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.vesalainen.parsers.sql.dsql.ui;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Category;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.PhoneNumber;
import com.google.appengine.api.datastore.PostalAddress;
import com.google.appengine.api.datastore.Rating;
import com.google.appengine.api.datastore.ShortBlob;
import com.google.appengine.api.datastore.Text;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Vector;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.vesalainen.parsers.magic.Magic;
import org.vesalainen.parsers.magic.Magic.MagicResult;

/**
 * @author Timo Vesalainen
 */
public class DSJTable extends JTable
{
    private static final Magic magic = Magic.newInstance();
    private static File currentDirectory;
    
    public DSJTable(Object[][] rowData, Object[] columnNames)
    {
        super(rowData, columnNames);
        init();
    }

    public DSJTable(Vector rowData, Vector columnNames)
    {
        super(rowData, columnNames);
        init();
    }

    public DSJTable(int numRows, int numColumns)
    {
        super(numRows, numColumns);
        init();
    }

    public DSJTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm)
    {
        super(dm, cm, sm);
        init();
    }

    public DSJTable(TableModel dm, TableColumnModel cm)
    {
        super(dm, cm);
        init();
    }

    public DSJTable(TableModel dm)
    {
        super(dm);
        init();
    }

    public DSJTable()
    {
        init();
    }

    private void init()
    {
        setRowSelectionAllowed(true);
        
        setDefaultEditor(ShortBlob.class, new ShortBlobCellEditor());
        setDefaultEditor(Blob.class, new BlobCellEditor());
        setDefaultEditor(Rating.class, new RatingCellEditor());
        setDefaultEditor(PostalAddress.class, new PostalAddressCellEditor());
        setDefaultEditor(PhoneNumber.class, new PhoneNumberCellEditor());
        setDefaultEditor(Link.class, new LinkCellEditor());
        setDefaultEditor(Text.class, new TextCellEditor());
        setDefaultEditor(Email.class, new EmailCellEditor());
        setDefaultEditor(Category.class, new CategoryCellEditor());
        
        setDefaultRenderer(ShortBlob.class, new ShortBlobTableCellRenderer());
        setDefaultRenderer(Blob.class, new BlobTableCellRenderer());
        setDefaultRenderer(Rating.class, new RatingTableCellRenderer());
        setDefaultRenderer(PostalAddress.class, new PostalAddressTableCellRenderer());
        setDefaultRenderer(PhoneNumber.class, new PhoneNumberTableCellRenderer());
        setDefaultRenderer(Link.class, new LinkTableCellRenderer());
        setDefaultRenderer(Text.class, new TextTableCellRenderer());
        setDefaultRenderer(Email.class, new EmailTableCellRenderer());
        setDefaultRenderer(Category.class, new CategoryTableCellRenderer());
    }

    public class ShortBlobCellEditor extends BlobCellEditor
    {
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
        {
            columnName = table.getColumnName(column);
            if (value != null)
            {
                ShortBlob blob = (ShortBlob) value;
                bytes = blob.getBytes();
                guess = magic.guess(bytes);
                button.setText(guess.getDescription());
            }
            return button;
        }

        @Override
        public Object getCellEditorValue()
        {
            if (bytes != null)
            {
                return new ShortBlob(bytes);
            }
            else
            {
                return null;
            }
        }

    }
    public class BlobCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener
    {
        protected static final String EDIT = "edit";
        protected JButton button = new JButton();
        protected BytesDialog dialog = new BytesDialog();
        protected byte[] bytes;
        protected MagicResult guess;
        protected String columnName;
        
        public BlobCellEditor()
        {
            button.setActionCommand(EDIT);
            button.addActionListener(this);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
        {
            columnName = table.getColumnName(column);
            if (value != null)
            {
                Blob blob = (Blob) value;
                bytes = blob.getBytes();
                guess = magic.guess(bytes);
                button.setText(guess.getDescription());
            }
            return button;
        }

        @Override
        public Object getCellEditorValue()
        {
            if (bytes != null)
            {
                return new Blob(bytes);
            }
            else
            {
                return null;
            }
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (EDIT.equals(e.getActionCommand()))
            {
                if (dialog.input())
                {
                    switch (dialog.getInput())
                    {
                        case LOAD:
                        {
                            JFileChooser fc = new JFileChooser();
                            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                            String[] extensions = guess.getExtensions();
                            if (extensions.length > 0 && !extensions[0].isEmpty())
                            {
                                FileFilter ff = new FileNameExtensionFilter(guess.getDescription(), guess.getExtensions());
                                fc.setFileFilter(ff);
                            }
                            if (currentDirectory != null)
                            {
                                fc.setCurrentDirectory(currentDirectory);
                            }
                            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
                            {
                                File file = fc.getSelectedFile();
                                long length = file.length();
                                if (length > 1000000)
                                {
                                    JOptionPane.showMessageDialog(null, file, "File is too big", JOptionPane.ERROR_MESSAGE);
                                }
                                else
                                {
                                    bytes = new byte[(int)length];
                                    try (FileInputStream fis = new FileInputStream(file))
                                    {
                                        fis.read(bytes);
                                        currentDirectory = file.getParentFile();
                                    }
                                    catch (IOException ex)
                                    {
                                        JOptionPane.showMessageDialog(null, ex.getLocalizedMessage());
                                    }
                                }
                            }
                        }
                            break;
                        case STORE:
                        {
                            JFileChooser fc = new JFileChooser();
                            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                            String[] extensions = guess.getExtensions();
                            if (extensions.length > 0 && !extensions[0].isEmpty())
                            {
                                FileFilter ff = new FileNameExtensionFilter(guess.getDescription(), guess.getExtensions());
                                fc.setFileFilter(ff);
                            }
                            if (currentDirectory != null)
                            {
                                fc.setCurrentDirectory(currentDirectory);
                            }
                            String suffix = "";
                            if (extensions.length > 0)
                            {
                                suffix = "."+extensions[0].toLowerCase();
                            }
                            fc.setSelectedFile(new File(columnName+suffix));
                            if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
                            {
                                File file = fc.getSelectedFile();
                                if (file != null)
                                {
                                    if (file.getName().indexOf('.') == -1)
                                    {
                                        file = new File(file.getPath() + suffix);
                                    }
                                    currentDirectory = file.getParentFile();
                                    if (file.exists())
                                    {
                                        int confirm = JOptionPane.showConfirmDialog(null, file, "File exists! Overwrite?", JOptionPane.OK_CANCEL_OPTION);
                                        if (JOptionPane.YES_OPTION == confirm)
                                        {
                                            return;
                                        }
                                    }
                                    try (FileOutputStream fos = new FileOutputStream(file))
                                    {
                                        fos.write(bytes);
                                    }
                                    catch (IOException ex)
                                    {
                                        JOptionPane.showMessageDialog(null, ex.getLocalizedMessage());
                                    }
                                }
                            }
                        }
                            break;
                        case OPEN:
                        {
                            String[] extensions = guess.getExtensions();
                            String suffix = "";
                            if (extensions.length > 0)
                            {
                                suffix = "."+extensions[0].toLowerCase();
                            }
                            try
                            {
                                    Path tempPath = Files.createTempFile(null, suffix);
                                    try (FileOutputStream fos = new FileOutputStream(tempPath.toFile()))
                                    {
                                        fos.write(bytes);
                                    }
                                    catch (IOException ex)
                                    {
                                        JOptionPane.showMessageDialog(null, ex.getLocalizedMessage());
                                    }
                                    ExternalEditor ee = new ExternalEditor(tempPath);
                                    ee.input();
                            }
                            catch (IOException ex)
                            {
                                JOptionPane.showMessageDialog(null, ex.getLocalizedMessage());
                            }
                        }
                            break;
                        case REMOVE:
                            bytes = null;
                            break;
                        case CANCEL:
                            break;
                    }
                    guess = magic.guess(bytes);
                    button.setText(guess.getDescription());
                }
                fireEditingStopped();
            }
        }
        
    }
    public class RatingCellEditor extends AbstractCellEditor implements TableCellEditor
    {
        private JSpinner editor;

        public RatingCellEditor()
        {
            SpinnerNumberModel model = new SpinnerNumberModel(0, Rating.MIN_VALUE, Rating.MAX_VALUE, 1);
            editor = new JSpinner(model);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
        {
            if (value != null)
            {
                Rating rating = (Rating) value;
                editor.setValue(rating.getRating());
            }
            return editor;
        }

        @Override
        public Object getCellEditorValue()
        {
            return new Rating((Integer)editor.getValue());
        }
        
    }
    public class PostalAddressCellEditor extends AbstractCellEditor implements TableCellEditor
    {
        private JTextField editor = new JTextField();

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
        {
            if (value != null)
            {
                PostalAddress addr = (PostalAddress) value;
                editor.setText(addr.getAddress());
            }
            return editor;
        }

        @Override
        public Object getCellEditorValue()
        {
            return new PostalAddress(editor.getText());
        }
        
    }
    public class PhoneNumberCellEditor extends AbstractCellEditor implements TableCellEditor
    {
        private JTextField editor = new JTextField();

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
        {
            if (value != null)
            {
                PhoneNumber phone = (PhoneNumber) value;
                editor.setText(phone.getNumber());
            }
            return editor;
        }

        @Override
        public Object getCellEditorValue()
        {
            return new PhoneNumber(editor.getText());
        }
        
    }
    public class LinkCellEditor extends AbstractCellEditor implements TableCellEditor
    {
        private JTextField editor = new JTextField();

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
        {
            if (value != null)
            {
                Link link = (Link) value;
                editor.setText(link.getValue());
            }
            return editor;
        }

        @Override
        public Object getCellEditorValue()
        {
            return new Link(editor.getText());
        }
        
    }
    public class TextCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener
    {
        private static final String EDIT = "edit";
        private JButton button = new JButton();
        private TextDialog dialog = new TextDialog();

        public TextCellEditor()
        {
            button.setActionCommand(EDIT);
            button.addActionListener(this);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
        {
            if (value != null)
            {
                Text text = (Text) value;
                button.setText(text.getValue());
            }
            return button;
        }

        @Override
        public Object getCellEditorValue()
        {
            return new Text(button.getText());
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (EDIT.equals(e.getActionCommand()))
            {
                dialog.setText(button.getText());
                if (dialog.input())
                {
                    button.setText(dialog.getText());
                }
                fireEditingStopped();
            }
        }
        
    }
    public class EmailCellEditor extends AbstractCellEditor implements TableCellEditor
    {
        private JTextField editor = new JTextField();

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
        {
            if (value != null)
            {
                Email email = (Email) value;
                editor.setText(email.getEmail());
            }
            return editor;
        }

        @Override
        public Object getCellEditorValue()
        {
            return new Email(editor.getText());
        }
        
    }
    public class CategoryCellEditor extends AbstractCellEditor implements TableCellEditor
    {
        private JTextField editor = new JTextField();

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
        {
            if (value != null)
            {
                Category category = (Category) value;
                editor.setText(category.getCategory());
            }
            return editor;
        }

        @Override
        public Object getCellEditorValue()
        {
            return new Category(editor.getText());
        }
        
    }
    public class BlobTableCellRenderer extends TooltippedTableCellRenderer
    {

        @Override
        protected void setValue(Object value)
        {
            if (value != null)
            {
                Blob blob = (Blob) value;
                MagicResult guess = magic.guess(blob.getBytes());
                if (guess != null)
                {
                    super.setValue(guess.getDescription());
                }
                else
                {
                    super.setValue("???");
                }
            }
            else
            {
                super.setValue(value);
            }
        }
    }

    public class ShortBlobTableCellRenderer extends TooltippedTableCellRenderer
    {

        @Override
        protected void setValue(Object value)
        {
            if (value != null)
            {
                ShortBlob blob = (ShortBlob) value;
                MagicResult guess = magic.guess(blob.getBytes());
                if (guess != null)
                {
                    super.setValue(guess.getDescription());
                }
                else
                {
                    super.setValue("???");
                }
            }
            else
            {
                super.setValue(value);
            }
        }
    }

    public class RatingTableCellRenderer extends TooltippedTableCellRenderer
    {

        @Override
        protected void setValue(Object value)
        {
            if (value != null)
            {
                Rating rating = (Rating) value;
                super.setValue(rating.getRating());
            }
            else
            {
                super.setValue(value);
            }
        }
    }

    public class PostalAddressTableCellRenderer extends TooltippedTableCellRenderer
    {

        @Override
        protected void setValue(Object value)
        {
            if (value != null)
            {
                PostalAddress address = (PostalAddress) value;
                super.setValue(address.getAddress());
            }
            else
            {
                super.setValue(value);
            }
        }
    }

    public class PhoneNumberTableCellRenderer extends TooltippedTableCellRenderer
    {

        @Override
        protected void setValue(Object value)
        {
            if (value != null)
            {
                PhoneNumber number = (PhoneNumber) value;
                super.setValue(number.getNumber());
            }
            else
            {
                super.setValue(value);
            }
        }
    }

    public class LinkTableCellRenderer extends TooltippedTableCellRenderer
    {

        @Override
        protected void setValue(Object value)
        {
            if (value != null)
            {
                Link link = (Link) value;
                super.setValue(link.getValue());
            }
            else
            {
                super.setValue(value);
            }
        }
    }

    public class TextTableCellRenderer extends TooltippedTableCellRenderer
    {

        @Override
        protected void setValue(Object value)
        {
            if (value != null)
            {
                Text text = (Text) value;
                super.setValue(text.getValue());
            }
            else
            {
                super.setValue(value);
            }
        }
    }

    public class EmailTableCellRenderer extends TooltippedTableCellRenderer
    {

        @Override
        protected void setValue(Object value)
        {
            if (value != null)
            {
                Email email = (Email) value;
                super.setValue(email.getEmail());
            }
            else
            {
                super.setValue(value);
            }
        }
    }
    public class CategoryTableCellRenderer extends TooltippedTableCellRenderer
    {

        @Override
        protected void setValue(Object value)
        {
            if (value != null)
            {
                Category category = (Category) value;
                super.setValue(category.getCategory());
            }
            else
            {
                super.setValue(value);
            }
        }
    }
    public class TooltippedTableCellRenderer extends DefaultTableCellRenderer
    {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setToolTipText(getText());
            return component;
        }
    }

}
