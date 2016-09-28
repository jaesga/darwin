<div class="row">
    <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
        #{form _action, class:'form-inline'}
            <div class="form-group">
                <select class="selectpicker" name="finder.field">
                    #{list items:_fieldsAllowed, as:'field' }
                        <option value="${field}" #{if _model != null && _model.field != null && _model.field.equals(field)}selected#{/if}>&{'finder.' + field}</option>
                    #{/list}
                </select>
            </div>
            <button type="submit" class="submit"><i class="material-icons">search</i></button>
        #{/form}
    </div>
</div>
