import React from 'react'
import {useParams} from 'react-router-dom'
import {allConfigs, initialConfigs} from '../../../constants/config'
import routes from '../../../constants/routes'
import EditFrame from '../../EditFrame'

const EditConfig = () => {
    const id = useParams()

    const inverseChecksSelected = (unusedChecks) => {
        if (!unusedChecks.includes('config-2') && !unusedChecks.includes('config-3')) {
            return true
        }

        if (!unusedChecks.includes('config-4') && !unusedChecks.includes('config-5')) {
            return true
        }

        if (!unusedChecks.includes('config-6') && !unusedChecks.includes('config-7')) {
            return true
        }

        if (!unusedChecks.includes('config-8') && !unusedChecks.includes('config-9')) {
            return true
        }

        if (!unusedChecks.includes('config-10') && !unusedChecks.includes('config-11')) {
            return true
        }

        return false
    }

    const getInverseChecks = (unusedChecks) => {
        if (!unusedChecks.includes('config-2') && !unusedChecks.includes('config-3')) {
            return [initialConfigs.configs['config-2'].content, initialConfigs.configs['config-3'].content]
        }

        if (!unusedChecks.includes('config-4') && !unusedChecks.includes('config-5')) {
            return [initialConfigs.configs['config-4'].content, initialConfigs.configs['config-5'].content]
        }

        if (!unusedChecks.includes('config-6') && !unusedChecks.includes('config-7')) {
           return [initialConfigs.configs['config-6'].content, initialConfigs.configs['config-7'].content]
        }

        if (!unusedChecks.includes('config-8') && !unusedChecks.includes('config-9')) {
            return [initialConfigs.configs['config-8'].content, initialConfigs.configs['config-9'].content]
        }

        if (!unusedChecks.includes('config-10') && !unusedChecks.includes('config-11')) {
        return [initialConfigs.configs['config-10'].content, initialConfigs.configs['config-11'].content]
        }

        return []
    }

    return (<EditFrame
        initialConfigs={initialConfigs}
        allConfigs={allConfigs}
        inverseChecksSelected={(unusedChecks) => inverseChecksSelected(unusedChecks)}
        getInverseChecks={(unusedChecks) => getInverseChecks(unusedChecks)}
        type={'java'}
        homeRoute={routes.HOME}
        id={id}
    />)
}

export default EditConfig



