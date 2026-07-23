import api from '.axios';


export function getRides(status){
    return api.get('rides', {params: status ? { status } : {} });
}

export function getRide(id){
    return api.get(`/rides/${id}`);
}

export function createRide(rideData){
    return api.post('/rides', rideData);
}

export function canceRide(id){
    return api.patch(`/rides/${id}/cancel`);
}
