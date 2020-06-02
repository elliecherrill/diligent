import React from 'react'
import * as API from '../../../api'
import ViewFrame from '../../ViewFrame'
import {downloadFile} from '../../../constants/util'
import {initialPythonConfigs} from '../../../constants/config'
import routes from '../../../constants/routes'


const ViewPythonConfigs = () => {
    const createFile = c => {
        API.get_python_checks_for_download(c['_id']['$oid']).then(response => downloadFile(response, 'diligent_py'))
    }

    return (<ViewFrame
        type={'python'}
        createFile={createFile}
        initialConfigs={initialPythonConfigs}
        editRoute={routes.EDIT_PYTHON_CONFIG}
    />)
}


export default ViewPythonConfigs