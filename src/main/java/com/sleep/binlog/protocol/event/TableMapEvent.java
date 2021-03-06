package com.sleep.binlog.protocol.event;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.sleep.binlog.protocol.ColumnType;
import com.sleep.binlog.protocol.Protocol;

/**
 * @author huangyafeng
 *
 *         <a href="http://dev.mysql.com/doc/internals/en/table-map-event.html">
 */
public class TableMapEvent extends Protocol {

	private long tableId;

	private int flags;

	private String schema;

	private String table;

	private long columnCount;

	private int[] columnTypeDef;

	private int[] columnMetaDef;

	private int[] nullBitmap;

	public TableMapEvent(ByteBuffer buf) throws IOException {
		super(buf);
		this.tableId = readLong(6);
		this.flags = readInt(2);
		readInt(1);
		this.schema = readZeroEndString();
		readInt(1);
		this.table = readZeroEndString();
		this.columnCount = readLengthEncodedInt();
		this.columnTypeDef = read((int) columnCount);
		readLengthEncodedInt();
		columnMetaDef = new int[(int) columnCount];
		// https://github.com/mysql/mysql-server/blob/e0e0ae2ea27c9bb76577664845507ef224d362e4/sql/field.cc
		// do_save_field_metadata
		for (int i = 0; i < columnCount; i++) {
			switch (columnTypeDef[i]) {
			case ColumnType.MYSQL_TYPE_VARCHAR:
			case ColumnType.MYSQL_TYPE_NEWDECIMAL:
			case ColumnType.MYSQL_TYPE_BIT:
				columnMetaDef[i] = readInt(2);
				break;
			case ColumnType.MYSQL_TYPE_BLOB:
			case ColumnType.MYSQL_TYPE_DOUBLE:
			case ColumnType.MYSQL_TYPE_FLOAT:
			case ColumnType.MYSQL_TYPE_GEOMETRY:
			case ColumnType.MYSQL_TYPE_TIME2:
			case ColumnType.MYSQL_TYPE_DATETIME2:
			case ColumnType.MYSQL_TYPE_TIMESTAMP2:
				columnMetaDef[i] = readInt(1);
				break;
			case ColumnType.MYSQL_TYPE_SET:
			case ColumnType.MYSQL_TYPE_ENUM:
			case ColumnType.MYSQL_TYPE_STRING:
				columnMetaDef[i] = readBigedianInt(2);
				break;
			default:
				columnMetaDef[i] = 0;
			}
		}
		this.nullBitmap = readBigedianBitmap((int) columnCount);
	}

	public long getTableId() {
		return tableId;
	}

	public void setTableId(long tableId) {
		this.tableId = tableId;
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public long getColumnCount() {
		return columnCount;
	}

	public void setColumnCount(long columnCount) {
		this.columnCount = columnCount;
	}

	public int[] getColumnTypeDef() {
		return columnTypeDef;
	}

	public void setColumnTypeDef(int[] columnTypeDef) {
		this.columnTypeDef = columnTypeDef;
	}

	public int[] getColumnMetaDef() {
		return columnMetaDef;
	}

	public void setColumnMetaDef(int[] columnMetaDef) {
		this.columnMetaDef = columnMetaDef;
	}

	public int[] getNullBitmap() {
		return nullBitmap;
	}

	public void setNullBitmap(int[] nullBitmap) {
		this.nullBitmap = nullBitmap;
	}

	@Override
	public String toString() {
		return "TableMapEvent [tableId=" + tableId + ", flags=" + flags + ", schema=" + schema + ", table=" + table
				+ ", columnCount=" + columnCount + ", columnTypeDef=" + Arrays.toString(columnTypeDef)
				+ ", columnMetaDef=" + Arrays.toString(columnMetaDef) + ", nullBitmap=" + Arrays.toString(nullBitmap)
				+ "]";
	}

}
