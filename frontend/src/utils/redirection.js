function performOrRedirect(response, onSuccessWithData, onFailure) {
    response.ok ? response.json().then(data => onSuccessWithData(data)) : onFailure()
}

export default performOrRedirect