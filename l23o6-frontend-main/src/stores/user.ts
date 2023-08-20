import { defineStore } from "pinia";
import { request } from "~/utils/request";

export const useUserStore = defineStore('user', {
    state: () => {
        return {
            username: '',
            name: '',
            type: '',
            idn: '',
            phone: '',
            MileagePoints: 0,
            DiscountPoints: 0,
        }
    },
    getters: {
        getUserName() {

        },
    },
    actions: {
        fetch() {
            request({
                url: '/user',
                method: 'GET'
            }).then((res) => {
                console.log(res);
                this.username = res.data.data.username;
                this.name = res.data.data.name;
                this.type = res.data.data.type;
                this.idn = res.data.data.idn;
                this.phone = res.data.data.phone;
                this.MileagePoints = res.data.data.mileage_points;
                console.log(res.data.data.MileagePoints)
                this.DiscountPoints = res.data.data.discount_points;
            }).catch((err) => {
                console.log(err)
            })
        }

    }

})