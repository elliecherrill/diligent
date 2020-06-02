import React from 'react'
import {useParams} from 'react-router-dom'
import {allPythonConfigs, initialPythonConfigs} from '../../../constants/config'
import routes from '../../../constants/routes'
import EditFrame from '../../EditFrame'

const EditPythonConfig = () => {
    const id = useParams()

    return (<EditFrame
        initialConfigs={initialPythonConfigs}
        allConfigs={allPythonConfigs}
        inverseChecksSelected={(unusedChecks) => {return false}}
        getInverseChecks={(unusedChecks) => {return []}}
        type={'python'}
        homeRoute={routes.PYTHON}
        id={id}
    />)
}

export default EditPythonConfig



