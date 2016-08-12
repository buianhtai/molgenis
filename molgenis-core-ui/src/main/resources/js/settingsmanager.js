(function ($, molgenis) {
    "use strict";

    var api = new molgenis.RestClient();

    $(function () {
        var onValueChange = function (event) {
            // check if user has read or write permission on entity
            api.getAsync('/api/v1/' + event.value.fullName + '/meta', {'expand': ['attributes']}).done(function (entity) {
                React.render(molgenis.ui.Form({
                    entity: entity,
                    entityInstance: event.value.simpleName,
                    mode: entity.writable ? 'edit' : 'view',
                    modal: false,
                    enableOptionalFilter: false,
                    enableFormIndex: false
                }), $('#settings-container')[0]);
            });
        };

        var EntitySelectBox = React.render(molgenis.ui.EntitySelectBox({
            entity: 'sys_md_entities',
            query: {
                operator: 'NESTED',
                nestedRules: [
                    {field: 'package', operator: 'EQUALS', value: 'sys_set'},
                    {operator: 'AND'},
                    {operator: 'NOT'},
                    {field: 'abstract', operator: 'EQUALS', value: 'true'}
                ]
            },
            mode: 'view',
            multiple: false,
            placeholder: 'Select application or plugin settings',
            focus: true,
            required: true, // do not show clear icon in select
            onValueChange: onValueChange
        }), $('#settings-select-container')[0]);

        // initialize with application settings
        onValueChange({
            value: {
                fullName: 'sys_set_app',
                simpleName: 'app'
            }
        });
    });
}($, window.top.molgenis = window.top.molgenis || {}));