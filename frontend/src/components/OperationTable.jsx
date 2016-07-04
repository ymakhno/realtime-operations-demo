import moment from 'moment';
import React from 'react';
import IconButton from 'material-ui/IconButton';
import RaisedButton from 'material-ui/RaisedButton';
import FontIcon from 'material-ui/FontIcon';
import {Table, TableBody, TableFooter, TableHeader, TableHeaderColumn, TableRow, TableRowColumn}
    from 'material-ui/Table';

const styles = {
    footerContent: {
        textAlign: 'right'
    },
    footerText: {
        float: 'right',
        paddingTop: '16px',
        height: '16px'
    },
    tableCell: {
        height: '24px'
    }
};

class PagingComponent extends React.Component {
    constructor(props) {
        super(props);
        this.state = {total: 0, columns: 0};

        this.props.streamHandler.addHandler(((e) => {
            this.setState({ total: e.total });
        }).bind(this));

        this.props.tableStructureHandler.addHandler((e => {
            this.setState({ columns: e.tableStructure.length })
        }).bind(this));
    }

    render() {
        let handler = this.props.streamHandler;
        let offset = handler.offset;
        let limit = handler.limit;
        let total = this.state.total;
        let colspanButton = Math.max(Math.floor(this.state.columns / 2), 1);
        let colspanPaging = Math.max(this.state.columns - colspanButton, 1);
        return (
                <TableRow>
                    <TableRowColumn colSpan={colspanButton} >
                        <RaisedButton label="Configure" primary={true} onClick={this.props.onConfigureClick}/>
                    </TableRowColumn>
                    <TableRowColumn colSpan={colspanPaging} style={styles.footerContent}>
                        <IconButton disabled={offset === 0}
                                    onClick={(e => handler.restartFromOffset(offset - 20)).bind(this)}>
                            <FontIcon className="material-icons">chevron_left</FontIcon>
                        </IconButton>
                        <IconButton disabled={offset + limit >= total}
                                    onClick={(e => handler.restartFromOffset(offset + 20)).bind(this)}>
                            <FontIcon className="material-icons">chevron_right</FontIcon>
                        </IconButton>
                        <div style={styles.footerText}>
                            {Math.min((offset + 1), total) + '-' + Math.min((offset + limit), total) + ' of ' + total}
                        </div>
                    </TableRowColumn>
                </TableRow>
        );
    }
}
PagingComponent.muiName = 'TableRow';


class OperationTable extends React.Component {

    constructor(props) {
        super(props);
        this.state = {tableData: [], tableStructure: []};

        this.props.streamHandler.addHandler(((e) => {
            this.setState({ tableData : e.data });
        }).bind(this));

        this.props.tableStructureHandler.addHandler((e => {
            this.setState({ tableStructure : e.tableStructure })
        }).bind(this));
    }

    render() {
        let onConfigureClick = this.props.onConfigureClick;
        let tableData = this.state.tableData;
        let tableStructure = this.state.tableStructure;

        return (
            <div>
                <Table>
                    <TableHeader displaySelectAll={false} adjustForCheckbox={false}>
                        <PagingComponent onConfigureClick={onConfigureClick}
                                         streamHandler={this.props.streamHandler}
                                         tableStructureHandler={this.props.tableStructureHandler}/>
                        <TableRow style={styles.tableCell}>
                            {tableStructure.map(field => (
                                <TableHeaderColumn style={styles.tableCell}>{field.label}</TableHeaderColumn>
                            ))}
                        </TableRow>
                    </TableHeader>
                    <TableBody displayRowCheckbox={false}>
                        {tableData.map((row, index) => (
                            <TableRow key={index} style={styles.tableCell}>
                                {tableStructure.map(field => (
                                    <TableRowColumn style={styles.tableCell}>
                                        {field.type === 'timestamp' ?
                                            moment(row[field.name]).format('HH:mm:ss.SSS') :
                                            row[field.name]}
                                    </TableRowColumn>
                                ))}
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </div>)
    }

    static _capitalize(str) {
        return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
    }
}

export default OperationTable;