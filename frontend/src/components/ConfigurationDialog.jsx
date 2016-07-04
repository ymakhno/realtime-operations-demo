import React from 'react';
import {Card, CardActions, CardHeader, CardText} from 'material-ui/Card';
import Checkbox from 'material-ui/Checkbox';
import Dialog from 'material-ui/Dialog';
import FlatButton from 'material-ui/FlatButton';

const styles = {
    card: {
        float: "left",
        width: '32%',
        marginLeft: "1%"
    },
    cardHeader: {
        paddingRight : "0"
    },
    title: {
        padding: "10px 24px 3px 24px"
    },
    content: {
        margin: "6px 0 0 0"
    }
};

class ConfigurationDialog extends React.Component {

    constructor(props) {
        super(props);

        this.props.dialogOpenHandler.addHandler(this._onOpen.bind(this));

        let allFields = {};
        let fieldsByOperations = this.props.fieldsByOperations;
        let defaultFields = this.props.defaultFields;
        Object.keys(fieldsByOperations).forEach(operation => {
            fieldsByOperations[operation].forEach(field => {
                let fieldName = field.name;
                if (!allFields.hasOwnProperty(fieldName)) {
                    allFields[fieldName] = {
                        name: fieldName,
                        label: field.label,
                        type: field.type,
                        operationTypes: [],
                        selected: false
                    };
                }
                allFields[fieldName].operationTypes.push(operation);
            })
        });
        defaultFields.forEach(f => allFields[f.name].selected = true);

        this.state = {opened: false, fields: allFields};
        this._onSubmit();
    }

    _selectCheckBox(fieldName) {
        let fieldsState = this.state.fields;
        fieldsState[fieldName].selected = !fieldsState[fieldName].selected;
        this.setState({fields: fieldsState});
    }

    _onOpen() {
        let fieldsSnapshot = JSON.parse(JSON.stringify(this.state.fields));
        this.setState({opened: true, fieldsSnapshot: fieldsSnapshot})
    }

    _onCancel() {
        this.setState({opened: false, fields: this.state.fieldsSnapshot})
    }

    _onSubmit() {
        let fieldsState = this.state.fields;
        let wsFields = {"DEPOSIT" : [], "WITHDRAWAL" : [], "TRANSFER" : []};
        let tableStructure = [];
        Object.keys(fieldsState).forEach(key => {
            let view = fieldsState[key];
            if (view.selected) {
                tableStructure.push({name : view.name, label : view.label, type : view.type});
                view.operationTypes.forEach(o => wsFields[o].push(view.name))
            }
        });
        this.props.tableStructureHandler.fire({tableStructure: tableStructure, wsFields: wsFields});
        this.setState({opened: false, fieldsSnapshot: this.state.fields})
    }

    render() {
        let fieldsByOperations = this.props.fieldsByOperations;
        let fieldsState = this.state.fields;
        var selected = 0;
        Object.keys(fieldsState).forEach(key => selected += (fieldsState[key].selected ? 1 : 0));
        let disable = selected >= 10;

        const tabs = [
            { operationType: "DEPOSIT", title: "Deposit Fields" },
            { operationType: "WITHDRAWAL", title: "Withdrawal Fields" },
            { operationType: "TRANSFER", title: "Transfer Fields" }
        ];
        const actions = [
            <FlatButton
                label="Cancel"
                primary={false}
                onClick={this._onCancel.bind(this)}
            />,
            <FlatButton
                label="Submit"
                primary={true}
                onClick={this._onSubmit.bind(this)}
            />
        ];
        return (
            <div>
            <Dialog
                title={'Configure Table (' + selected + ' of 10)'}
                modal={false}
                open={this.state.opened}
                actions={actions}
                titleStyle={styles.title}
                onRequestClose={this.handleClose}
                autoScrollBodyContent={true}>
                <div style={styles.content}>
                    {tabs.map(tab => (
                        <Card style={styles.card}>
                            <CardHeader textStyle={styles.cardHeader} title={tab.title} />
                            <CardText>
                                {fieldsByOperations[tab.operationType].map((field) => (
                                    <Checkbox
                                        disabled={!fieldsState[field.name].selected && disable}
                                        label={field.label}
                                        checked={fieldsState[field.name].selected}
                                        onCheck={(e => this._selectCheckBox(field.name)).bind(this)}
                                    />
                                ))}
                            </CardText>
                        </Card>
                    ))}
                </div>
            </Dialog>
            </div>
        )
    }
}

export default ConfigurationDialog;