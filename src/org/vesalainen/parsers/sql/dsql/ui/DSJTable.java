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
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.PhoneNumber;
import com.google.appengine.api.datastore.PostalAddress;
import com.google.appengine.api.datastore.Rating;
import com.google.appengine.api.datastore.ShortBlob;
import com.google.appengine.api.datastore.Text;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.vesalainen.parsers.magic.Magic;
import org.vesalainen.parsers.magic.Magic.MagicResult;
import org.vesalainen.parsers.sql.dsql.DSQLParser;

/**
 * @author Timo Vesalainen
 */
public class DSJTable extends JTable
{
    private static final Pattern NUMERIC = Pattern.compile("[0-9\\,\\.\\- ]+");
    private static final Magic magic = Magic.getInstance();
    private static final DSQLParser parser = DSQLParser.getInstance();
    
    private Window owner;
    
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

    public void setOwner(Window owner)
    {
        this.owner = owner;
    }

    private void init()
    {
        setRowSelectionAllowed(true);
        
        setDefaultEditor(GeoPt.class, new GeoPtCellEditor());
        setDefaultEditor(ShortBlob.class, new ShortBlobCellEditor());
        setDefaultEditor(Blob.class, new BlobCellEditor());
        setDefaultEditor(Rating.class, new RatingCellEditor());
        setDefaultEditor(PostalAddress.class, new PostalAddressCellEditor());
        setDefaultEditor(PhoneNumber.class, new PhoneNumberCellEditor());
        setDefaultEditor(Link.class, new LinkCellEditor());
        setDefaultEditor(Text.class, new TextCellEditor());
        setDefaultEditor(Email.class, new EmailCellEditor());
        setDefaultEditor(Category.class, new CategoryCellEditor());
        
        setDefaultRenderer(GeoPt.class, new GeoPtTableCellRenderer());
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

    @Override
    public void print(Graphics g)
    {
        int totalColumnWidth = columnModel.getTotalColumnWidth();
        Graphics2D gg = (Graphics2D) g;
        FontRenderContext fontRenderContext = gg.getFontRenderContext();
        for (int col=0;col<columnModel.getColumnCount();col++)
        {
            TableColumn column = columnModel.getColumn(col);
            int max = 0;
            boolean numeric = true;
            for (int row=0;row<getRowCount();row++)
            {
                Object value = dataModel.getValueAt(row, col);
                String str = value != null ? value.toString() : "";
                Matcher matcher = NUMERIC.matcher(str);
                if (!matcher.matches())
                {
                    numeric = false;
                }
                TableCellRenderer cellRenderer = getCellRenderer(row, col);
                Component component = cellRenderer.getTableCellRendererComponent(this, value, false, false, row, col);
                Font font = component.getFont();
                Rectangle2D stringBounds = font.getStringBounds(str, fontRenderContext);
                max = Math.max(max, (int)(1.5*stringBounds.getWidth()));
            }
            if (numeric)
            {
                column.setMaxWidth(max);
            }
            else
            {
                column.setMinWidth(0);
                column.setMaxWidth(max);
            }
        }
        int left = totalColumnWidth - columnModel.getTotalColumnWidth();
        int hiddenTotal = 0;
        for (int col=0;col<columnModel.getColumnCount();col++)
        {
            TableColumn column = columnModel.getColumn(col);
            hiddenTotal += column.getMaxWidth()-column.getWidth();
        }
        float ratio = (float)left/(float)hiddenTotal;
        for (int col=0;col<columnModel.getColumnCount();col++)
        {
            TableColumn column = columnModel.getColumn(col);
            int hidden = column.getMaxWidth()-column.getWidth();
            if (hidden > 0)
            {
                column.setMinWidth(column.getWidth()+(int)(ratio*(float)hidden));
            }
        }
        totalColumnWidth = columnModel.getTotalColumnWidth();
        revalidate();
        super.paint(g);
    }

    public class GeoPtCellEditor extends AbstractCellEditor implements TableCellEditor
    {
        private JTextField editor = new JTextField();

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
        {
            if (value != null)
            {
                GeoPt pt = (GeoPt) value;
                editor.setText(DSJTable.toString(pt));
            }
            else
            {
                editor.setText(null);
            }
            return editor;
        }

        @Override
        public Object getCellEditorValue()
        {
            String text = editor.getText();
            if (text != null && !text.isEmpty())
            {
                return parser.parseCoordinate(editor.getText(), null);
            }
            else
            {
                return null;
            }
        }
        
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
                dialog.setBytes(blob.getBytes());
                button.setText(dialog.getContentDescription());
            }
            else
            {
                button.setText(null);
            }
            return button;
        }

        @Override
        public Object getCellEditorValue()
        {
            byte[] bytes = dialog.getBytes();
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
        protected BytesDialog dialog = new BytesDialog(owner);
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
                dialog.setBytes(blob.getBytes());
                button.setText(dialog.getContentDescription());
            }
            else
            {
                button.setText(null);
            }
            return button;
        }

        @Override
        public Object getCellEditorValue()
        {
            byte[] bytes = dialog.getBytes();
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
                    button.setText(dialog.getContentDescription());
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
            else
            {
                editor.setValue(0);
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
            else
            {
                editor.setText(null);
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
            else
            {
                editor.setText(null);
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
            else
            {
                editor.setText(null);
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
        private TextDialog dialog = new TextDialog(owner);

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
            else
            {
                button.setText(null);
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
            else
            {
                editor.setText(null);
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
            else
            {
                editor.setText(null);
            }
            return editor;
        }

        @Override
        public Object getCellEditorValue()
        {
            return new Category(editor.getText());
        }
        
    }
    public class GeoPtTableCellRenderer extends TooltippedTableCellRenderer
    {

        @Override
        protected void setValue(Object value)
        {
            if (value != null)
            {
                GeoPt pt = (GeoPt) value;
                super.setValue(toString(pt));
            }
            else
            {
                super.setValue(value);
            }
        }
    }

    public static String toString(GeoPt pt)
    {
        float lat = pt.getLatitude();
        char ns = lat > 0 ? 'N' : 'S';
        lat = Math.abs(lat);
        int lati = (int) lat;
        lat = lat-lati;
        float lon = pt.getLongitude();
        char we = lon > 0 ? 'E' : 'W';
        lon = Math.abs(lon);
        int loni = (int) lon;
        lon = lon-loni;
        return String.format(Locale.US,
                "%c %d\u00b0 %.3f', %c %d\u00b0 %.3f'", 
                ns,
                lati,
                lat*60,
                we,
                loni,
                lon*60
                );
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
