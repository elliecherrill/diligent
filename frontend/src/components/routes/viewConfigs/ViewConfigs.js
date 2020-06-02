import React from 'react'
import * as API from '../../../api'
import ViewFrame from '../../ViewFrame'
import {downloadFile} from '../../../constants/util'
import {initialConfigs} from '../../../constants/config'
import routes from '../../../constants/routes'


const ViewConfigs = () => {
    const createFile = c => {
        API.get_checks_for_download(c['_id']['$oid']).then(response => downloadFile(response, 'diligent'))
    }

    return (<ViewFrame
        type={'java'}
        createFile={createFile}
        initialConfigs={initialConfigs}
        editRoute={routes.EDIT_CONFIG}
    />)
}


export default ViewConfigs